package click.dobel.tado.exporter.apiclient.auth

import click.dobel.tado.exporter.apiclient.TadoConfigurationProperties
import click.dobel.tado.exporter.apiclient.auth.model.request.TadoAuthLoginRequest
import click.dobel.tado.exporter.apiclient.auth.model.request.TadoAuthRefreshRequest
import click.dobel.tado.exporter.apiclient.auth.model.response.TadoAuthResponse
import click.dobel.tado.util.logger
import org.springframework.http.HttpRequest
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import java.util.concurrent.atomic.AtomicReference

@Component
class TadoAuthFilter(
  private val configuration: TadoConfigurationProperties,
  private val authClient: AuthClient,
) : ClientHttpRequestInterceptor {

  private companion object {
    private val LOGGER = logger()
  }

  private val lastAuthResponse = AtomicReference<TadoAuthResponse?>()

  override fun intercept(
    request: HttpRequest,
    body: ByteArray,
    execution: ClientHttpRequestExecution
  ): ClientHttpResponse {
    request.headers.accept = listOf(MediaType.APPLICATION_JSON)
    request.headers.setBearerAuth(getAccessToken())
    return execution.execute(request, body)
  }

  private fun updateLastAuthResponse(response: TadoAuthResponse): String {
    lastAuthResponse.set(response)
    return response.accessToken
  }

  private fun getAccessToken(): String {
    val auth = lastAuthResponse.get()

    // TODO: how to synchronize parallel requests properly in Rx
    return when {
      auth == null -> newAuth()
        .run { updateLastAuthResponse(this) }

      auth.isExpired() -> refreshAuth(auth.refreshToken)
        .run { updateLastAuthResponse(this) }

      else -> auth.accessToken
    }
  }

  private fun newAuth(): TadoAuthResponse {
    LOGGER.info("Obtaining new bearer token for {}.", configuration.username)
    return authClient.token(TadoAuthLoginRequest(configuration))
  }

  private fun refreshAuth(refreshToken: String): TadoAuthResponse {
    LOGGER.info("Refreshing bearer token.")
    return try {
      authClient.token(TadoAuthRefreshRequest(configuration, refreshToken))
    } catch (ex: RestClientException) {
      LOGGER.warn("Refreshing bearer token failed.", ex)
      newAuth()
    }
  }
}
