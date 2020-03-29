package click.dobel.tado.client

import click.dobel.tado.client.auth.AuthClient
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import io.micronaut.http.MediaType
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID
import javax.inject.Singleton

@Singleton
class WireMockSupport(
  configuration: TadoConfiguration
) {
  val port = configuration.authServer.getPort()
  val server: WireMockServer = WireMockServer(port)

  init {
    server.start()
  }

  // TODO test on random port
  val random: () -> Double = Math::random
  private fun randomPort(min: Int, max: Int): Int {
    return (min + random() * (max - min)).toInt()
  }
}

private fun String.getPort() =
  this
    .replaceBeforeLast(":", "")
    .removePrefix(":")
    .toInt()

fun RequestPatternBuilder.withFormParam(name: String, value: String): RequestPatternBuilder =
  this.withRequestBody(containing("${name}=${URLEncoder.encode(value, StandardCharsets.UTF_8)}"))

fun successfulAuthMapping(
  accessToken: String = "accessToken",
  tokenType: String = "bearer",
  refreshToken: String = "refreshToken",
  expiresIn: Int = 1337,
  scope: String = "test.scope",
  jti: String = UUID.randomUUID().toString()
) =
  post(AuthClient.TOKEN_PATH)
    .willReturn(aResponse()
      .withStatus(200)
      .withHeader("Content-Type", MediaType.APPLICATION_JSON)
      .withBody("""
        {
            "access_token": "$accessToken",
            "refresh_token": "$refreshToken",
            "token_type": "$tokenType",
            "expires_in": $expiresIn,
            "scope": "$scope",
            "jti": "$jti"
        }
        """.trimIndent())
    )
