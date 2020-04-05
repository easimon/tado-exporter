package click.dobel.tado.test

import click.dobel.tado.client.TadoConfiguration
import click.dobel.tado.client.auth.AuthClient
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID
import javax.inject.Singleton

@Singleton
internal class WireMockSupport(
  configuration: TadoConfiguration
) {
  val port = configuration.authServer.getPort()
  val server: WireMockServer = WireMockServer(port)

  // TODO test on random port
  val random: () -> Double = Math::random
  private fun randomPort(min: Int, max: Int): Int {
    return (min + random() * (max - min)).toInt()
  }

  private fun String.getPort() =
    this
      .replaceBeforeLast(":", "")
      .removePrefix(":")
      .toInt()
}

fun RequestPatternBuilder.withFormParam(name: String, value: String): RequestPatternBuilder =
  this.withRequestBody(containing("${name}=${URLEncoder.encode(value, StandardCharsets.UTF_8)}"))

private fun ResponseDefinitionBuilder.applicationJson(): ResponseDefinitionBuilder {
  return withHeader("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8")
}

internal object AuthMock {
  const val DEFAULT_ACCESS_TOKEN = "accessToken"
  const val DEFAULT_TOKEN_TYPE = "bearer"
  const val DEFAULT_REFRESH_TOKEN = "refreshToken"
  const val DEFAULT_EXPIRES_IN = 1337L
  const val DEFAULT_SCOPE = "test.scope"
  val DEFAULT_JTI = UUID.randomUUID().toString()

  fun successfulUsernamePasswordAuthMapping(
    accessToken: String = DEFAULT_ACCESS_TOKEN,
    tokenType: String = DEFAULT_TOKEN_TYPE,
    refreshToken: String = DEFAULT_REFRESH_TOKEN,
    expiresIn: Long = DEFAULT_EXPIRES_IN,
    scope: String = DEFAULT_SCOPE,
    jti: String = DEFAULT_JTI
  ) =
    post(AuthClient.TOKEN_PATH)
      .withRequestBody(containing("grant_type=password"))
      .willReturn(aResponse()
        .applicationJson()
        .withStatus(HttpStatus.OK.code)
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

  fun failedRefreshAuthMapping() =
    post(AuthClient.TOKEN_PATH)
      .withRequestBody(containing("grant_type=refresh_token"))
      .willReturn(aResponse()
        .withStatus(HttpStatus.BAD_REQUEST.code)
        .applicationJson()
        .withBody("""
        {
            "error": "invalid_grant",
            "error_description": "Bad credentials"
        }
      """.trimIndent())
      )
}
