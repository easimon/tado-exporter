package click.dobel.tado.exporter.apiclient.auth

import click.dobel.tado.exporter.apiclient.TadoConfigurationProperties
import click.dobel.tado.exporter.apiclient.auth.model.accesstoken.AccessToken
import click.dobel.tado.exporter.apiclient.auth.model.accesstoken.toAccessToken
import click.dobel.tado.exporter.apiclient.auth.model.refreshtoken.RefreshToken
import click.dobel.tado.exporter.apiclient.auth.model.refreshtoken.RefreshTokenStore
import click.dobel.tado.exporter.apiclient.auth.model.refreshtoken.toRefreshToken
import click.dobel.tado.exporter.apiclient.auth.model.response.TadoAuthResponse
import click.dobel.tado.exporter.apiclient.auth.model.response.TadoDeviceAuthResponse
import click.dobel.tado.exporter.apiclient.auth.thread.PollingThreadContainer
import click.dobel.tado.exporter.apiclient.auth.thread.TransientPollingError
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Component
class TadoAuthenticator(
  configuration: TadoConfigurationProperties,
  private val authenticationClient: TadoAuthenticationClient
) {

  companion object {
    val logger = KotlinLogging.logger {}
  }

  private val refreshTokenStore = RefreshTokenStore.create(configuration.authCachePath)
  private val authenticationState: AtomicReference<TadoAuthenticationState> =
    AtomicReference(TadoAuthenticationState.Initial)

  val currentState: TadoAuthenticationState get() = updateStateMachine()
  val currentStateAsString: String get() = currentState.description
  val accessToken: AccessToken
    get() = currentState.let {
      when (it) {
        is TadoAuthenticationState.Authenticated -> it.accessToken
        else -> AccessToken.None
      }
    }

  private fun initiateAuthentication(): TadoAuthenticationState {
    val refreshToken = refreshTokenStore.token

    return if (refreshToken == null) {
      logger.info { "Initiating device code flow for authentication." }

      try {
        val deviceAuthResponse = authenticationClient.deviceAuthorization()
        logger.info { "Device code flow initialized, visit ${deviceAuthResponse.verificationUriComplete} to log in." }
        TadoAuthenticationState.PollingForDeviceCodeToken(deviceAuthResponse, authenticationClient)
      } catch (ex: Exception) {
        logger.error(ex) { "Initiating device code flow failed." }
        return TadoAuthenticationState.Error(ex)
      }
    } else {
      logger.info { "Trying persisted refresh token for authentication." }

      refresh(refreshToken)
    }
  }

  private fun updateStateMachine(): TadoAuthenticationState {
    return authenticationState.updateAndGet { old ->
      var state = old
      var couldAdvance = false

      do {
        advanceStep(state).also {
          couldAdvance = it.first
          state = it.second
        }
      } while (couldAdvance)

      state
    }
  }

  private fun advanceStep(oldState: TadoAuthenticationState): Pair<Boolean, TadoAuthenticationState> {
    return when (oldState) {
      is TadoAuthenticationState.Initial -> initiateAuthentication()

      is TadoAuthenticationState.PollingForDeviceCodeToken -> oldState.result
        .also {
          if (it is TadoAuthenticationState.Authenticated) {
            refreshTokenStore.token = it.refreshToken
          }
        }

      is TadoAuthenticationState.DelayingRetry -> oldState.result

      is TadoAuthenticationState.Authenticated -> refreshIfExpired(oldState)

      is TadoAuthenticationState.Error -> initiateDelayedRetry()
    }.let { newState ->
      (newState != oldState) to newState
    }.also { (advanced, newState) ->
      if (advanced) {
        logger.info {
          "Authentication state changed from ${oldState.description} to ${newState.description}"
        }
      } else {
        logger.trace {
          "State ${newState.description} unchanged."
        }
      }
    }
  }

  private fun refreshIfExpired(state: TadoAuthenticationState.Authenticated): TadoAuthenticationState {
    return if (!state.accessToken.isExpired)
      state
    else
      refresh(state.refreshToken)
  }

  private fun refresh(refreshToken: RefreshToken): TadoAuthenticationState {
    return try {
      logger.info { "Refreshing authentication token." }
      val response = authenticationClient.refresh(refreshToken)
      refreshTokenStore.token = response.toRefreshToken()
      TadoAuthenticationState.Authenticated(response)
    } catch (ex: TransientPollingError) {
      logger.error(ex) { "Authentication token refresh failed." }
      TadoAuthenticationState.Initial
    } catch (ex: Exception) {
      logger.error(ex) { "Authentication token refresh failed, discarding persisted refresh token." }
      refreshTokenStore.token = null
      TadoAuthenticationState.Error(ex)
    }
  }

  private fun initiateDelayedRetry(duration: Duration = 15.seconds): TadoAuthenticationState {
    logger.info { "Delaying next authentication attempt by $duration." }
    return TadoAuthenticationState.DelayingRetry(duration)
  }
}

sealed class TadoAuthenticationState(private val plainDescription: String) {
  data object Initial : TadoAuthenticationState("Authentication not started.")

  val description: String get() = "[$plainDescription]"

  data class PollingForDeviceCodeToken(
    val deviceAuthResponse: TadoDeviceAuthResponse,
    private val authClient: TadoAuthenticationClient
  ) : TadoAuthenticationState(
    "Device code authorization pending, " +
      "polling every ${deviceAuthResponse.pollingInterval.inWholeSeconds} seconds, " +
      "Visit ${deviceAuthResponse.verificationUriComplete} to log in, " +
      "expiring at ${deviceAuthResponse.expiresAt}."
  ) {
    private val pollingThread: PollingThreadContainer = PollingThreadContainer(
      name = "Device code polling thread",
      interval = deviceAuthResponse.pollingInterval,
      immediately = true,
    ) {
      if (deviceAuthResponse.isExpired)
        Initial
      else
        Authenticated(authClient.token(deviceAuthResponse.deviceCode))
    }

    val result get() = pollingThread.result ?: this
  }

  data class Authenticated(val accessToken: AccessToken.BearerToken, val refreshToken: RefreshToken) :
    TadoAuthenticationState("Authenticated, token expires at ${accessToken.expiresAt}.") {
    constructor(authResponse: TadoAuthResponse) : this(authResponse.toAccessToken(), authResponse.toRefreshToken())
  }

  data class DelayingRetry(val duration: Duration) : TadoAuthenticationState(
    "Delaying retry for ${duration.inWholeSeconds} seconds."
  ) {
    private val pollingThread: PollingThreadContainer = PollingThreadContainer(
      name = "Delay thread",
      interval = duration,
      immediately = false,
    ) {
      Initial
    }

    val result get() = pollingThread.result ?: this
  }

  data class Error(val exception: Exception) : TadoAuthenticationState(
    "Error: ${exception.message}."
  )
}
