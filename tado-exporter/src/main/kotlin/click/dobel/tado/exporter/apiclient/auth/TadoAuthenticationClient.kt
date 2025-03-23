package click.dobel.tado.exporter.apiclient.auth

import click.dobel.tado.exporter.apiclient.TadoConfigurationProperties
import click.dobel.tado.exporter.apiclient.auth.model.request.TadoAuthRefreshRequest
import click.dobel.tado.exporter.apiclient.auth.model.request.TadoAuthRequest
import click.dobel.tado.exporter.apiclient.auth.model.request.TadoAuthTokenRequest
import click.dobel.tado.exporter.apiclient.auth.model.request.TadoDeviceAuthRequest
import click.dobel.tado.exporter.apiclient.auth.model.response.TadoAuthResponse
import click.dobel.tado.exporter.apiclient.auth.model.response.TadoDeviceAuthResponse
import click.dobel.tado.exporter.apiclient.logging.loggingIfConfigured
import click.dobel.tado.exporter.apiclient.postForObject
import click.dobel.tado.exporter.ratelimit.OncePerIntervalRateLimiter
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Component
class TadoAuthenticationClient(
  private val configuration: TadoConfigurationProperties,
  restTemplateBuilder: RestTemplateBuilder,
) {

  companion object {
    internal const val TOKEN_PATH = "/oauth2/token"
    internal const val DEVICE_AUTHORIZATION_PATH = "/oauth2/device_authorize"
  }

  private val restTemplate: RestTemplate = restTemplateBuilder
    .loggingIfConfigured(configuration)
    .build()

  private val deviceAuthorizationRatelimiter = OncePerIntervalRateLimiter("New device code", 1.minutes)
  private val deviceCodeTokenPollingRateLimiter = OncePerIntervalRateLimiter("Device code polling", 1.seconds)
  private val tokenRefreshRateLimiter = OncePerIntervalRateLimiter("Access token refresh code polling", 1.minutes)

  private fun token(auth: TadoAuthRequest): TadoAuthResponse {
    return restTemplate.postForObject(
      "${configuration.authServer}$TOKEN_PATH",
      auth.asRequestEntity()
    )
  }

  fun token(deviceCode: String): TadoAuthResponse {
    return deviceCodeTokenPollingRateLimiter.executeRateLimited {
      token(TadoAuthTokenRequest(configuration, deviceCode))
    }
  }

  fun refresh(refreshToken: String): TadoAuthResponse {
    return tokenRefreshRateLimiter.executeRateLimited {
      token(TadoAuthRefreshRequest(configuration, refreshToken))
    }
  }

  fun deviceAuthorization(): TadoDeviceAuthResponse {
    return deviceAuthorizationRatelimiter.executeRateLimited {
      restTemplate.postForObject(
        "${configuration.authServer}$DEVICE_AUTHORIZATION_PATH",
        TadoDeviceAuthRequest(configuration).asRequestEntity()
      )
    }
  }
}
