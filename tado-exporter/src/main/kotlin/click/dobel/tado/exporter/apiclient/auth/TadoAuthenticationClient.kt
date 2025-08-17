package click.dobel.tado.exporter.apiclient.auth

import click.dobel.tado.exporter.apiclient.TadoConfigurationProperties
import click.dobel.tado.exporter.apiclient.auth.model.refreshtoken.RefreshToken
import click.dobel.tado.exporter.apiclient.auth.model.request.TadoAuthRefreshRequest
import click.dobel.tado.exporter.apiclient.auth.model.request.TadoAuthRequest
import click.dobel.tado.exporter.apiclient.auth.model.request.TadoAuthTokenRequest
import click.dobel.tado.exporter.apiclient.auth.model.request.TadoDeviceAuthRequest
import click.dobel.tado.exporter.apiclient.auth.model.response.TadoAuthResponse
import click.dobel.tado.exporter.apiclient.auth.model.response.TadoDeviceAuthResponse
import click.dobel.tado.exporter.apiclient.auth.thread.TransientPollingError
import click.dobel.tado.exporter.apiclient.bodyOrError
import click.dobel.tado.exporter.apiclient.logging.loggingIfConfigured
import click.dobel.tado.exporter.apiclient.userAgent
import click.dobel.tado.exporter.ratelimit.RateLimiter
import click.dobel.tado.exporter.ratelimit.RateLimiterFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.server.ServerErrorException
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Component
class TadoAuthenticationClient(
  private val configuration: TadoConfigurationProperties,
  rateLimiterFactory: RateLimiterFactory,
  restTemplateBuilder: RestTemplateBuilder,
) {

  companion object {
    internal const val TOKEN_PATH = "/oauth2/token"
    internal const val DEVICE_AUTHORIZATION_PATH = "/oauth2/device_authorize"
  }

  val r = RestClient.builder()

  private val restClient: RestClient = RestClient.create(
    restTemplateBuilder
      .loggingIfConfigured(configuration)
      .userAgent()
      .build()
  )

  private val deviceAuthorizationRatelimiter = rateLimiterFactory("New device code", 1.minutes)
  private val deviceCodeTokenPollingRateLimiter = rateLimiterFactory("Device code polling", 1.seconds)
  private val tokenRefreshRateLimiter = rateLimiterFactory("Access token refresh code polling", 1.minutes)

  private fun <T> execute(rateLimiter: RateLimiter, errorTranslator: ErrorTranslator, block: () -> T): T {
    return translateError(errorTranslator) {
      rateLimiter.executeRateLimited {
        block()
      }
    }
  }

  private fun <T> translateError(errorTranslator: ErrorTranslator, block: () -> T): T {
    return try {
      block()
    } catch (ex: TransientPollingError) {
      throw ex
    } catch (ex: Exception) {
      errorTranslator(ex)
    }
  }

  private fun token(auth: TadoAuthRequest): TadoAuthResponse {
    return restClient
      .post()
      .uri("${configuration.authServer}$TOKEN_PATH")
      .body(auth.asMultiValueMap())
      .retrieve()
      .bodyOrError()
  }

  fun token(deviceCode: String): TadoAuthResponse {
    fun errorTranslator(exception: Exception): Nothing {
      throw when (exception) {
        is ResourceAccessException,
        is HttpServerErrorException -> TransientPollingError(exception)

        is HttpClientErrorException -> {
          // TODO: proper error parsing
          if (exception.responseBodyAsString.contains("authorization_pending"))
            TransientPollingError(exception)
          else
            exception
        }

        else -> exception
      }
    }

    return execute(deviceCodeTokenPollingRateLimiter, ::errorTranslator) {
      token(TadoAuthTokenRequest(configuration, deviceCode))
    }
  }

  fun refresh(refreshToken: RefreshToken): TadoAuthResponse {
    fun errorTranslator(exception: Exception): Nothing {
      throw when (exception) {
        is ResourceAccessException,
        is ServerErrorException -> TransientPollingError(exception)

        else -> exception
      }
    }

    return execute(tokenRefreshRateLimiter, ::errorTranslator) {
      token(TadoAuthRefreshRequest(configuration, refreshToken.value))
    }
  }

  fun deviceAuthorization(): TadoDeviceAuthResponse {
    fun errorTranslator(exception: Exception): Nothing {
      throw when (exception) {
        is ResourceAccessException,
        is ServerErrorException -> TransientPollingError(exception)

        else -> exception
      }
    }

    return execute(deviceAuthorizationRatelimiter, ::errorTranslator) {
      val result: TadoDeviceAuthResponse = restClient
        .post()
        .uri("${configuration.authServer}$DEVICE_AUTHORIZATION_PATH")
        .body(TadoDeviceAuthRequest(configuration).asMultiValueMap())
        .retrieve()
        .bodyOrError()

      result
    }
  }
}

typealias ErrorTranslator = (exception: Exception) -> Nothing


