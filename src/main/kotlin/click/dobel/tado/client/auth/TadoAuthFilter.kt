package click.dobel.tado.client.auth

import click.dobel.tado.client.TadoApiClient
import click.dobel.tado.client.TadoConfiguration
import click.dobel.tado.logger
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.annotation.Filter
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.filter.ClientFilterChain
import io.micronaut.http.filter.HttpClientFilter
import org.reactivestreams.Publisher

@Filter(TadoApiClient.BASE_URL + "/**", serviceId = [TadoApiClient.SERVER])
class TadoAuthFilter(
  private val authClient: AuthClient,
  private val tadoConfiguration: TadoConfiguration
) : HttpClientFilter {

  private companion object {
    private val LOGGER = logger()
  }

  private var lastAuthResponse: TadoAuthResponse? = null

  override fun doFilter(request: MutableHttpRequest<*>, chain: ClientFilterChain): Publisher<out HttpResponse<*>> {
    return chain.proceed(request.bearerAuth(getAccessToken()))
  }

  private fun getAccessToken(): String {
    synchronized(this) {
      val auth = lastAuthResponse.let {
        when {
          it == null -> newAuth()
          it.isExpired() -> refreshAuth(it.refreshToken)
          else -> it
        }
      }
      lastAuthResponse = auth
      return auth.accessToken
    }
  }

  private fun newAuth(): TadoAuthResponse {
    LOGGER.info("Obtaining new bearer token for {}.", tadoConfiguration.username)
    try {
      return authClient.token(TadoAuthRequest.TadoAuthLoginRequest(tadoConfiguration))
    } catch (e: HttpClientResponseException) {
      LOGGER.error("Failed to authenticate, httpStatus ({})", e.status)
      throw e
    }
  }

  private fun refreshAuth(refreshToken: String): TadoAuthResponse {
    return try {
      LOGGER.info("Refreshing bearer token.")
      authClient.token(TadoAuthRequest.TadoAuthRefreshRequest(tadoConfiguration, refreshToken))
    } catch (e: HttpClientResponseException) {
      LOGGER.warn("Refreshing bearer token failed.", e)
      newAuth()
    }
  }
}
