package click.dobel.tado.exporter.apiclient.auth

import click.dobel.tado.exporter.apiclient.TadoConfigurationProperties
import click.dobel.tado.exporter.apiclient.auth.model.request.TadoAuthLoginRequest
import click.dobel.tado.exporter.apiclient.auth.model.request.TadoAuthRefreshRequest
import click.dobel.tado.exporter.apiclient.auth.model.response.TadoAuthResponse
import mu.KLogging
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

  private companion object : KLogging()

  private val lastAuthResponse = AtomicReference<TadoAuthResponse?>()

  override fun intercept(
    request: HttpRequest,
    body: ByteArray,
    execution: ClientHttpRequestExecution
  ): ClientHttpResponse {
    request.headers.setBearerAuth(getAccessToken())
    request.headers.accept = listOf(MediaType.APPLICATION_JSON)
    return execution.execute(request, body)
  }

  private fun updateLastAuthResponse(response: TadoAuthResponse): String {
    lastAuthResponse.set(response)
    logger.info { "New bearer token obtained, expires in ${response.expiresIn} seconds, at ${response.expiresAt}." }
    return response.accessToken
  }

  private fun getAccessToken(): String {
    val auth = lastAuthResponse.get()

    return when {
      auth == null -> newAuth()
        .run { updateLastAuthResponse(this) }

      auth.isExpired() -> refreshAuth(auth.refreshToken)
        .run { updateLastAuthResponse(this) }

      else -> auth.accessToken
    }
  }

  private fun newAuth(): TadoAuthResponse {
    logger.info { "Obtaining new bearer token for ${configuration.username}." }
    return authClient.token(TadoAuthLoginRequest(configuration))
  }

  private fun refreshAuth(refreshToken: String): TadoAuthResponse {
    logger.info("Refreshing bearer token.")
    return try {
      authClient.token(TadoAuthRefreshRequest(configuration, refreshToken))
    } catch (ex: RestClientException) {
      logger.warn(ex) { "Refreshing bearer token failed." }
      newAuth()
    }
  }
}
