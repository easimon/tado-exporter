package click.dobel.tado.exporter.test

import click.dobel.tado.exporter.apiclient.TadoConfigurationProperties
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders.copyOf
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response
import com.github.tomakehurst.wiremock.http.Response.Builder.like
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class WireMockSupport(
  configuration: TadoConfigurationProperties
) {
  private val authServerUrl = configuration.authServer
  val authServer = closingWireMockServer(authServerUrl.host, authServerUrl.port)

  private val apiServerUrl = configuration.apiServer
  val apiServer = closingWireMockServer(apiServerUrl.host, apiServerUrl.port)

  private val String.port: Int
    get() {
      val uri = URI.create(this)

      return if (uri.port >= 0) {
        uri.port
      } else when (uri.scheme) {
        "http" -> 80
        "https" -> 443
        else -> throw IllegalArgumentException("Unsupported test URL: $this")
      }
    }

  private val String.host: String
    get() = URI.create(this).host
}

private fun closingWireMockServer(host: String, port: Int): WireMockServer {
  return WireMockServer(
    wireMockConfig()
      .port(port)
      .bindAddress(host)
      .extensions(ConnectionCloseExtension())
  )
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
  return withHeader(HttpHeaders.CONTENT_TYPE, "${MediaType.APPLICATION_JSON_VALUE};charset=UTF-8")
}

private class ConnectionCloseExtension : ResponseTransformer() {
  override fun transform(request: Request?, response: Response, files: FileSource?, parameters: Parameters?): Response =
    like(response)
      .headers(
        copyOf(response.headers)
          .plus(HttpHeader(HttpHeaders.CONNECTION, "Close"))
      )
      .build()

  override fun getName() = "ConnectionCloseExtension"
}
