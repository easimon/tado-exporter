package click.dobel.tado.exporter.test

import click.dobel.tado.exporter.apiclient.DEFAULT_ACCESS_TOKEN
import click.dobel.tado.exporter.apiclient.DEFAULT_DEVICE_CODE
import click.dobel.tado.exporter.apiclient.DEFAULT_EXPIRES_IN_SECONDS
import click.dobel.tado.exporter.apiclient.DEFAULT_REFRESH_TOKEN
import click.dobel.tado.exporter.apiclient.DEFAULT_SCOPE
import click.dobel.tado.exporter.apiclient.DEFAULT_TOKEN_TYPE
import click.dobel.tado.exporter.apiclient.DEFAULT_USER_ID
import click.dobel.tado.exporter.apiclient.REFRESHED_ACCESS_TOKEN
import click.dobel.tado.exporter.apiclient.auth.TadoAuthenticationClient
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.HttpStatus

object AuthMockMappings {
  fun successfulDeviceAuthorization(): MappingBuilder =
    WireMock.post(TadoAuthenticationClient.DEVICE_AUTHORIZATION_PATH)
      .withRequestBody(WireMock.containing("client_id=").and(WireMock.containing("scope=")))
      .willReturn(
        WireMock.aResponse()
          .applicationJson()
          .withStatus(HttpStatus.OK.value())
          .withBody(
            """
              {
                "device_code": "$DEFAULT_DEVICE_CODE",
                "expires_in": 300,
                "interval": 0,
                "user_code": "ABC1DE",
                "verification_uri": "https://login.tado.com/oauth2/device",
                "verification_uri_complete":"https://login.tado.com/oauth2/device?user_code=ABC1DE"
              }
            """.trimIndent()
          )
      )

  fun successfulDeviceCodeTokenFetch() =
    WireMock.post(TadoAuthenticationClient.TOKEN_PATH)
      .withRequestBody(WireMock.containing("device_code=$DEFAULT_DEVICE_CODE"))
      .willReturn(
        WireMock.aResponse()
          .applicationJson()
          .withStatus(HttpStatus.OK.value())
          .withBody(
            """
              {
                  "access_token": "$DEFAULT_ACCESS_TOKEN",
                  "expires_in": $DEFAULT_EXPIRES_IN_SECONDS,
                  "refresh_token": "$DEFAULT_REFRESH_TOKEN",
                  "scope": "$DEFAULT_SCOPE",
                  "token_type": "$DEFAULT_TOKEN_TYPE",
                  "userId": "$DEFAULT_USER_ID"
              }
            """.trimIndent()
          )
      )

  fun successfulRefreshTokenFetch() =
    WireMock.post(TadoAuthenticationClient.TOKEN_PATH)
      .withRequestBody(
        WireMock.containing("grant_type=refresh_token").and(
          WireMock.containing("refresh_token=$DEFAULT_REFRESH_TOKEN")
        )
      )
      .willReturn(
        WireMock.aResponse()
          .withStatus(HttpStatus.OK.value())
          .applicationJson()
          .withBody(
            """
              {
                  "access_token": "$REFRESHED_ACCESS_TOKEN",
                  "expires_in": $DEFAULT_EXPIRES_IN_SECONDS,
                  "refresh_token": "$DEFAULT_REFRESH_TOKEN",
                  "scope": "$DEFAULT_SCOPE",
                  "token_type": "$DEFAULT_TOKEN_TYPE",
                  "userId": "$DEFAULT_USER_ID"
              }
            """.trimIndent()
          )
      )

  fun failedRefreshTokenFetch() =
    WireMock.post(TadoAuthenticationClient.TOKEN_PATH)
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
