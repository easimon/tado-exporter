package click.dobel.tado.exporter.apiclient.auth

import click.dobel.tado.exporter.apiclient.TadoConfigurationProperties
import click.dobel.tado.exporter.apiclient.auth.model.response.TadoAuthResponse
import click.dobel.tado.exporter.apiclient.auth.model.response.TadoDeviceAuthResponse
import mu.KLogging
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Component
class TadoAuthenticationState(
  private val configuration: TadoConfigurationProperties,
  private val authenticationClient: TadoAuthenticationClient
) {

  companion object : KLogging()

  private val authenticationState = AtomicReference<AuthenticationState>(AuthenticationState.Initial)
  private val refreshTokenFile = File(configuration.authCachePath)

  init {
    try {
      if (!refreshTokenFile.exists()) {
        refreshTokenFile.parentFile.mkdirs()
        refreshTokenFile.createNewFile()
      } else {
        refreshTokenFile.setWritable(true)
        val refreshToken = refreshTokenFile.readText()
        if (refreshToken.isNotEmpty()) {
          val state = AuthenticationState.Authenticated(
            TadoAuthResponse.fromPersistedRefreshToken(refreshToken),
            refreshTokenFile
          )
          authenticationState.set(state)
        }
      }
    } catch (ex: IOException) {
      logger.error(ex) { "Failed to access refresh token file: ${ex.message}" }
    }
  }

  val accessToken: AccessToken
    get() = updateStateMachine().let {
      when (it) {
        is AuthenticationState.Authenticated -> AccessToken.BearerToken(it.authResponse.accessToken)
        else -> AccessToken.None
      }
    }

  val currentStateAsString: String get() = updateStateMachine().description
  val currentState: AuthenticationState get() = updateStateMachine()

  private fun initiateDeviceCodeFlow(): AuthenticationState {
    logger.info { "Initiating device code flow." }

    return try {
      val deviceAuthResponse = authenticationClient.deviceAuthorization()
      logger.info { "Device code flow initialized, visit ${deviceAuthResponse.verificationUriComplete} to log in." }
      createTokenPoller(deviceAuthResponse.pollingInterval).start()
      AuthenticationState.PollingForDeviceCodeToken(deviceAuthResponse)
    } catch (exception: Exception) {
      logger.error(exception) { "Initiating device code flow failed." }
      AuthenticationState.AuthenticationError(exception)
    }
  }

  private fun updateStateMachine(): AuthenticationState {
    return authenticationState.updateAndGet { state ->
      when (state) {
        is AuthenticationState.PollingForDeviceCodeToken -> state
        is AuthenticationState.DelayingRetry -> state

        is AuthenticationState.Initial -> initiateDeviceCodeFlow()

        is AuthenticationState.Authenticated -> refreshIfExpired(state)

        is AuthenticationState.AuthenticationError -> initiateDelayedRetry()
      }
    }
  }

  private fun refreshIfExpired(state: AuthenticationState.Authenticated): AuthenticationState {
    if (!state.authResponse.isExpired) return state

    return try {
      logger.info { "Refreshing expired authentication token." }
      AuthenticationState.Authenticated(authenticationClient.refresh(state.authResponse.refreshToken), refreshTokenFile)
    } catch (e: Exception) {
      logger.error(e) { "Authentication token refresh failed." }
      AuthenticationState.AuthenticationError(e)
    }
  }

  private fun initiateDelayedRetry(duration: Duration = 1.minutes): AuthenticationState {
    Thread(createWaiter(duration)).start()
    return AuthenticationState.DelayingRetry(duration)
  }

  private fun createWaiter(duration: Duration): Thread {
    return Runnable {
      logger.info { "Waiting for ${duration.inWholeSeconds} seconds before restarting authentication flow." }
      Thread.sleep(duration.inWholeMilliseconds)
      authenticationState.set(initiateDeviceCodeFlow())
    }.asThread("Authentication Waiter")
  }

  private fun createTokenPoller(initialDelay: Duration): Thread {
    return Runnable {
      logger.info { "Waiting for device code authentication." }
      Thread.sleep(initialDelay.inWholeMilliseconds)
      var state = authenticationState.get()

      while (state is AuthenticationState.PollingForDeviceCodeToken) {
        val deviceCode = state.deviceAuthResponse.deviceCode
        val interval = state.deviceAuthResponse.pollingInterval

        try {
          val authResponse = authenticationClient.token(deviceCode)
          logger.info { "Authentication successful." }
          authenticationState.set(AuthenticationState.Authenticated(authResponse, refreshTokenFile))
        } catch (e: Exception) {
          if (state.deviceAuthResponse.isExpired) {
            logger.warn { "Device code authentication expired." }
            authenticationState.set(AuthenticationState.Initial)
          } else {
            logger.info { "${state.description} Reason: ${e::class}(${e.message})." }
            Thread.sleep(interval.inWholeMilliseconds)
          }
        }

        state = authenticationState.get()
      }

      logger.info { "Finished polling for device code token." }
    }.asThread("Authentication Poller")
  }
}

sealed class AuthenticationState(val description: String) {
  companion object {
    protected val logger = KotlinLogging.logger {}
  }

  data object Initial : AuthenticationState("Authentication not started")
  data class PollingForDeviceCodeToken(val deviceAuthResponse: TadoDeviceAuthResponse) : AuthenticationState(
    "Device code authorization pending, " +
      "polling every ${deviceAuthResponse.pollingInterval.inWholeSeconds} seconds, " +
      "Visit ${deviceAuthResponse.verificationUriComplete} to log in, " +
      "expiring at ${deviceAuthResponse.expiresAt}."
  )

  data class AuthenticationError(val exception: Exception) : AuthenticationState(
    "Authentication error: ${exception.message}."
  )

  data class DelayingRetry(val duration: Duration) : AuthenticationState(
    "Delaying retry for ${duration.inWholeSeconds} seconds."
  )

  data class Authenticated(val authResponse: TadoAuthResponse, val refreshTokenFile: File) : AuthenticationState(
    "Authenticated as ${authResponse.userId}, token expires at ${authResponse.expiresAt}."
  ) {
    init {
      try {
        refreshTokenFile.writeText(authResponse.refreshToken)
      } catch (e: IOException) {
        logger.error(e) { "Failed to persist refresh token." }
      }
    }
  }
}

fun Runnable.asThread(name: String) = Thread(this, name)

sealed class AccessToken {
  data object None : AccessToken()
  data class BearerToken(val value: String) : AccessToken()
}
