package click.dobel.tado.client.auth

import click.dobel.tado.client.TadoApiClient
import click.dobel.tado.client.TadoConfiguration
import click.dobel.tado.client.auth.request.TadoAuthLoginRequest
import click.dobel.tado.client.auth.request.TadoAuthRefreshRequest
import click.dobel.tado.client.auth.response.TadoAuthResponse
import click.dobel.tado.util.logger
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.annotation.Filter
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.filter.ClientFilterChain
import io.micronaut.http.filter.HttpClientFilter
import jakarta.inject.Provider
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono
import java.util.concurrent.atomic.AtomicReference
import reactor.kotlin.core.publisher.toMono

@Filter(TadoApiClient.BASE_URL + "/**", serviceId = ["tado-api"])
class TadoAuthFilter(
  private val authClient: Provider<AuthClient>,
  private val tadoConfiguration: TadoConfiguration
) : HttpClientFilter {

  private companion object {
    private val LOGGER = logger()
  }

  private val lastAuthResponse = AtomicReference<TadoAuthResponse?>()

  override fun doFilter(request: MutableHttpRequest<*>, chain: ClientFilterChain): Publisher<out HttpResponse<*>> {
    return getAccessToken().flatMap {
        token -> chain.proceed(request.bearerAuth(token)).toMono()
    }
  }

  private fun updateLastAuthResponse(response: TadoAuthResponse): String {
    lastAuthResponse.set(response)
    return response.accessToken
  }

  private fun getAccessToken(): Mono<String> {
    val auth = lastAuthResponse.get()

    // TODO: how to synchronize parallel requests properly in Rx
    return when {
      auth == null -> newAuth()
        .map { updateLastAuthResponse(it) }

      auth.isExpired() -> refreshAuth(auth.refreshToken)
        .map { updateLastAuthResponse(it) }

      else -> Mono.just(auth.accessToken)
    }
  }

  private fun newAuth(): Mono<TadoAuthResponse> {
    LOGGER.info("Obtaining new bearer token for {}.", tadoConfiguration.username)
    return authClient.get()
      .token(TadoAuthLoginRequest(tadoConfiguration))
      .doOnError { e ->
        when (e) {
          is HttpClientResponseException -> LOGGER.error("Failed to authenticate, httpStatus ({})", e.status)
          else -> LOGGER.error("Failed to authenticate, unexpected error.", e)
        }
      }
  }

  private fun refreshAuth(refreshToken: String): Mono<TadoAuthResponse> {
    LOGGER.info("Refreshing bearer token.")
    return authClient.get()
      .token(TadoAuthRefreshRequest(tadoConfiguration, refreshToken))
      .onErrorResume { e ->
        LOGGER.warn("Refreshing bearer token failed.", e)
        newAuth()
      }
  }
}
