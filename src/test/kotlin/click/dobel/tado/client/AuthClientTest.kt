package click.dobel.tado.client

import click.dobel.tado.client.auth.AuthClient
import click.dobel.tado.client.auth.TadoAuthRequest
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.micronaut.test.annotation.MicronautTest

@MicronautTest
internal class AuthClientTest(
  private val authClient: AuthClient,
  private val configuration: TadoConfiguration,
  private val mock: WireMockSupport
) : StringSpec({

  "authenticates correctly" {
    val result = authClient.token(TadoAuthRequest.TadoAuthLoginRequest(configuration))

    result.accessToken shouldBe "accessToken"
    result.tokenType shouldBe "bearer"
    result.refreshToken shouldBe "refreshToken"
    result.expiresIn shouldBe 1337
    result.scope shouldBe "test.scope"

    mock.server.verify(
      1,
      postRequestedFor(urlEqualTo(AuthClient.TOKEN_PATH))
        .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
        .withFormParam("client_id", configuration.clientId)
        .withFormParam("client_secret", configuration.clientSecret)
        .withFormParam("scope", configuration.scope)
        .withFormParam("username", configuration.username)
        .withFormParam("password", configuration.password)
        .withFormParam("grant_type", "password")
    )

    mock.server.allServeEvents.size shouldBe 1
  }

}) {
  override fun beforeTest(testCase: TestCase) {
    mock.server.stubFor(successfulAuthMapping())
  }

  override fun afterTest(testCase: TestCase, result: TestResult) {
    mock.server.findAllUnmatchedRequests().size shouldBe 0
    mock.server.resetAll()
  }
}
