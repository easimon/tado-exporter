package click.dobel.tado.exporter.apiclient.auth

import click.dobel.tado.exporter.apiclient.TadoConfigurationProperties
import click.dobel.tado.exporter.apiclient.auth.model.request.TadoAuthRequest
import click.dobel.tado.exporter.apiclient.auth.model.response.TadoAuthResponse
import click.dobel.tado.exporter.apiclient.postForObject
import click.dobel.tado.exporter.ratelimit.RateLimiter
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class AuthClient(
  private val configuration: TadoConfigurationProperties,
  private val authRateLimiter: RateLimiter,
  restTemplateBuilder: RestTemplateBuilder,
) {

  companion object {
    internal const val SERVICE_ID = "tado-auth"
    internal const val TOKEN_PATH = "/oauth/token"
  }

  private val restTemplate: RestTemplate = restTemplateBuilder
    .build()

  fun token(auth: TadoAuthRequest): TadoAuthResponse {
    return authRateLimiter.executeRateLimited {
      restTemplate.postForObject(
        "${configuration.authServer}$TOKEN_PATH",
        auth.asRequestEntity()
      )
    }
  }
}
