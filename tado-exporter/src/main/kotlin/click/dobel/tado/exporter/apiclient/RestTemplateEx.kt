package click.dobel.tado.exporter.apiclient

import click.dobel.tado.exporter.apiclient.auth.AccessToken
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient.RequestHeadersSpec
import org.springframework.web.client.RestTemplate

inline fun <reified T : Any> RestTemplate.getForEntity(
  url: String,
  vararg uriVariables: Any
): ResponseEntity<T> {
  return getForEntity(url, T::class.java, uriVariables)
}

inline fun <reified T : Any> RestTemplate.getForObject(
  url: String,
  vararg uriVariables: Any
): T {
  return getForObject(url, T::class.java, uriVariables)
    ?: throw ResourceAccessException("I/O error on GET $url: null response.")
}

inline fun <reified R : Any, reified T : Any> RestTemplate.postForObject(
  url: String,
  body: R,
  vararg uriVariables: Any
): T {
  return postForObject(url, body, T::class.java, uriVariables)
    ?: throw ResourceAccessException("I/O error on POST $url: null response.")
}

fun <S : RequestHeadersSpec<S>> RequestHeadersSpec<S>.userAgent() =
  header(HttpHeaders.USER_AGENT, "tado-exporter (https://github.com/easimon/tado-exporter)")

fun <S : RequestHeadersSpec<S>> RequestHeadersSpec<S>.bearerAuth(token: AccessToken.BearerToken) =
  header(HttpHeaders.AUTHORIZATION, "Bearer ${token.value}")
