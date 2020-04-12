package click.dobel.tado.client.auth

import click.dobel.tado.client.TadoConfiguration
import click.dobel.tado.test.AuthMockMappings
import click.dobel.tado.test.WireMockSupport
import click.dobel.tado.test.withFormParam
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import io.micronaut.http.HttpHeaders
import io.micronaut.http.MediaType
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

@MicronautTest
internal class AuthClientIntegrationTest(
  authClient: AuthClient,
  configuration: TadoConfiguration,
  private val mock: WireMockSupport
) : StringSpec({

  "authenticates correctly" {
    val result = authClient.token(TadoAuthRequest.TadoAuthLoginRequest(configuration))

    result.accessToken shouldBe AuthMockMappings.DEFAULT_ACCESS_TOKEN
    result.tokenType shouldBe AuthMockMappings.DEFAULT_TOKEN_TYPE
    result.refreshToken shouldBe AuthMockMappings.DEFAULT_REFRESH_TOKEN
    result.expiresIn shouldBe AuthMockMappings.DEFAULT_EXPIRES_IN
    result.scope shouldBe AuthMockMappings.DEFAULT_SCOPE
    result.jti shouldBe AuthMockMappings.DEFAULT_JTI

    mock.authServer.verify(
      1,
      postRequestedFor(urlEqualTo(AuthClient.TOKEN_PATH))
        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_FORM_URLENCODED))
        .withFormParam(TadoAuthRequest.P_CLIENT_ID, configuration.clientId)
        .withFormParam(TadoAuthRequest.P_CLIENT_SECRET, configuration.clientSecret)
        .withFormParam(TadoAuthRequest.P_SCOPE, configuration.scope)
        .withFormParam(TadoAuthRequest.P_USERNAME, configuration.username)
        .withFormParam(TadoAuthRequest.P_PASSWORD, configuration.password)
        .withFormParam(TadoAuthRequest.P_GRANT_TYPE, TadoAuthRequest.P_GRANT_TYPE_PASSWORD)
    )

    mock.authServer.allServeEvents.size shouldBe 1
  }

  "refresh with invalid refresh token fails" {
    val REFRESH_TOKEN = "invalidRefreshToken"

    mock.authServer.stubFor(AuthMockMappings.failedRefreshAuthMapping())

    shouldThrow<HttpClientResponseException> {
      authClient.token(
        TadoAuthRequest.TadoAuthRefreshRequest(configuration, REFRESH_TOKEN)
      )
    }

    mock.authServer.verify(
      1,
      postRequestedFor(urlEqualTo(AuthClient.TOKEN_PATH))
        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_FORM_URLENCODED))
        .withFormParam(TadoAuthRequest.P_CLIENT_ID, configuration.clientId)
        .withFormParam(TadoAuthRequest.P_CLIENT_SECRET, configuration.clientSecret)
        .withFormParam(TadoAuthRequest.P_SCOPE, configuration.scope)
        .withFormParam(TadoAuthRequest.P_REFRESH_TOKEN, REFRESH_TOKEN)
        .withFormParam(TadoAuthRequest.P_GRANT_TYPE, TadoAuthRequest.P_GRANT_TYPE_REFRESH_TOKEN)
    )

    mock.authServer.allServeEvents.size shouldBe 1
  }

}) {
  override fun beforeTest(testCase: TestCase) {
    mock.authServer.start()
    mock.authServer.stubFor(AuthMockMappings.successfulUsernamePasswordAuthMapping())
  }

  override fun afterTest(testCase: TestCase, result: TestResult) {
    try {
      mock.authServer.findAllUnmatchedRequests().size shouldBe 0
    } finally {
      mock.authServer.resetAll()
      mock.authServer.shutdown()
    }
  }
}
