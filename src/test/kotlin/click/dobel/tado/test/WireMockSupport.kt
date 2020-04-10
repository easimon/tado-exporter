package click.dobel.tado.test

import click.dobel.tado.client.TadoConfiguration
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import io.micronaut.http.HttpHeaders
import io.micronaut.http.MediaType
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Singleton

@Singleton
internal class WireMockSupport(
  configuration: TadoConfiguration
) {
  private val authPort = configuration.authServer.getPort()
  val authServer = WireMockServer(authPort)

  private val apiPort = configuration.apiServer.getPort()
  val apiServer = WireMockServer(apiPort)

  private fun String.getPort(): Int {
    val uri = URI.create(this)

    return if (uri.port >= 0) {
      uri.port
    } else when (uri.scheme) {
      "http" -> 80
      "https" -> 443
      else -> throw IllegalArgumentException("Unsupported test URL: $this")
    }
  }
}

private fun utf8(value: String) =
  URLEncoder.encode(value, StandardCharsets.UTF_8.name())

internal fun RequestPatternBuilder.withFormParam(name: String, value: String) =
  withRequestBody(
    containing("${utf8(name)}=${utf8(value)}")
  )

internal fun RequestPatternBuilder.withBearerAuth(accessToken: String) =
  withHeader(
    HttpHeaders.AUTHORIZATION,
    equalTo("Bearer $accessToken")
  )

internal fun ResponseDefinitionBuilder.applicationJson(): ResponseDefinitionBuilder {
  return withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON + ";charset=UTF-8")
}
