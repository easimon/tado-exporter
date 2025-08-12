package click.dobel.tado.exporter.apiclient

import click.dobel.tado.exporter.apiclient.auth.model.accesstoken.AccessToken
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpHeaders
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClient.RequestHeadersSpec

fun RestTemplateBuilder.userAgent(): RestTemplateBuilder =
  defaultHeader(HttpHeaders.USER_AGENT, "tado-exporter (https://github.com/easimon/tado-exporter)")

fun <S : RequestHeadersSpec<S>> RequestHeadersSpec<S>.bearerAuth(token: AccessToken.BearerToken) =
  header(HttpHeaders.AUTHORIZATION, "Bearer ${token.value}")

inline fun <reified T> RestClient.ResponseSpec.bodyOrError(): T =
  body(T::class.java) ?: throw ResourceAccessException("HTTP request returned null")
