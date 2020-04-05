package click.dobel.tado.client.auth

import click.dobel.tado.client.TadoConfiguration
import click.dobel.tado.test.AuthMock
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
internal class AuthClientTest(
  authClient: AuthClient,
  configuration: TadoConfiguration,
  private val mock: WireMockSupport
) : StringSpec({

  "authenticates correctly" {
    val result = authClient.token(TadoAuthRequest.TadoAuthLoginRequest(configuration))

    result.accessToken shouldBe AuthMock.DEFAULT_ACCESS_TOKEN
    result.tokenType shouldBe AuthMock.DEFAULT_TOKEN_TYPE
    result.refreshToken shouldBe AuthMock.DEFAULT_REFRESH_TOKEN
    result.expiresIn shouldBe AuthMock.DEFAULT_EXPIRES_IN
    result.scope shouldBe AuthMock.DEFAULT_SCOPE

    mock.server.verify(
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

    mock.server.allServeEvents.size shouldBe 1
  }

  "refresh with invalid refresh token fails" {
    val REFRESH_TOKEN = "invalidRefreshToken"

    mock.server.stubFor(AuthMock.failedRefreshAuthMapping())

    shouldThrow<HttpClientResponseException> {
      authClient.token(
        TadoAuthRequest.TadoAuthRefreshRequest(configuration, REFRESH_TOKEN)
      )
    }

    mock.server.verify(
      1,
      postRequestedFor(urlEqualTo(AuthClient.TOKEN_PATH))
        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_FORM_URLENCODED))
        .withFormParam(TadoAuthRequest.P_CLIENT_ID, configuration.clientId)
        .withFormParam(TadoAuthRequest.P_CLIENT_SECRET, configuration.clientSecret)
        .withFormParam(TadoAuthRequest.P_SCOPE, configuration.scope)
        .withFormParam(TadoAuthRequest.P_REFRESH_TOKEN, REFRESH_TOKEN)
        .withFormParam(TadoAuthRequest.P_GRANT_TYPE, TadoAuthRequest.P_GRANT_TYPE_REFRESH_TOKEN)
    )

    mock.server.allServeEvents.size shouldBe 1
  }

}) {
  override fun beforeTest(testCase: TestCase) {
    mock.server.start()
    mock.server.stubFor(AuthMock.successfulUsernamePasswordAuthMapping())
  }

  override fun afterTest(testCase: TestCase, result: TestResult) {
    mock.server.findAllUnmatchedRequests().size shouldBe 0
    mock.server.resetAll()
    mock.server.shutdown()
  }
}
