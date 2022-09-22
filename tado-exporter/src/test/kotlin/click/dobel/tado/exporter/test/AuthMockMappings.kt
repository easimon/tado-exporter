package click.dobel.tado.exporter.test

import click.dobel.tado.exporter.apiclient.auth.AuthClient
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.HttpStatus
import java.util.UUID

object AuthMockMappings {
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
    WireMock.post(AuthClient.TOKEN_PATH)
      .withRequestBody(WireMock.containing("grant_type=password"))
      .willReturn(
        WireMock.aResponse()
          .applicationJson()
          .withStatus(HttpStatus.OK.value())
          .withBody(
            """
              {
                  "access_token": "$accessToken",
                  "refresh_token": "$refreshToken",
                  "token_type": "$tokenType",
                  "expires_in": $expiresIn,
                  "scope": "$scope",
                  "jti": "$jti"
              }
            """.trimIndent()
          )
      )

  fun failedRefreshAuthMapping() =
    WireMock.post(AuthClient.TOKEN_PATH)
      .withRequestBody(WireMock.containing("grant_type=refresh_token"))
      .willReturn(
        WireMock.aResponse()
          .withStatus(HttpStatus.BAD_REQUEST.value())
          .applicationJson()
          .withBody(
            """
              {
                  "error": "invalid_grant",
                  "error_description": "Bad credentials"
              }
            """.trimIndent()
          )
      )
}
