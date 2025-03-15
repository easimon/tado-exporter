package click.dobel.tado.exporter.apiclient.auth

import click.dobel.tado.exporter.apiclient.TadoConfigurationProperties
import click.dobel.tado.exporter.apiclient.auth.AuthClientIntegrationTest.AuthClientIntegrationTestConfig
import click.dobel.tado.exporter.apiclient.auth.model.request.TadoAuthLoginRequest
import click.dobel.tado.exporter.apiclient.auth.model.request.TadoAuthRefreshRequest
import click.dobel.tado.exporter.apiclient.auth.model.request.TadoAuthRequest
import click.dobel.tado.exporter.ratelimit.RateLimiter
import click.dobel.tado.exporter.test.AuthMockMappings
import click.dobel.tado.exporter.test.IntegrationTest
import click.dobel.tado.exporter.test.WireMockSupport
import click.dobel.tado.exporter.test.withFormParam
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.HttpClientErrorException

@IntegrationTest
@Import(AuthClientIntegrationTestConfig::class)
class AuthClientIntegrationTest(
  authClient: AuthClient,
  configuration: TadoConfigurationProperties,
  private val mock: WireMockSupport
) : StringSpec({

  beforeTest {
    mock.authServer.start()
    mock.authServer.stubFor(AuthMockMappings.successfulUsernamePasswordAuthMapping())

  }

  afterTest {
    try {
      mock.authServer.findAllUnmatchedRequests().size shouldBe 0
    } finally {
      mock.authServer.resetAll()
      mock.authServer.shutdown()
    }
  }

  "authenticates correctly" {
    val result = authClient.token(TadoAuthLoginRequest(configuration))

    result.accessToken shouldBe AuthMockMappings.DEFAULT_ACCESS_TOKEN
    result.tokenType shouldBe AuthMockMappings.DEFAULT_TOKEN_TYPE
    result.refreshToken shouldBe AuthMockMappings.DEFAULT_REFRESH_TOKEN
    result.expiresIn shouldBe AuthMockMappings.DEFAULT_EXPIRES_IN
    result.scope shouldBe AuthMockMappings.DEFAULT_SCOPE
    result.jti shouldBe AuthMockMappings.DEFAULT_JTI

    mock.authServer.verify(
      1,
      postRequestedFor(urlEqualTo(AuthClient.TOKEN_PATH))
        .withHeader(HttpHeaders.CONTENT_TYPE, matching("${MediaType.APPLICATION_FORM_URLENCODED_VALUE}.*"))
        .withFormParam(TadoAuthRequest.P_CLIENT_ID, configuration.clientId)
        .withFormParam(TadoAuthRequest.P_CLIENT_SECRET, configuration.clientSecret)
        .withFormParam(TadoAuthRequest.P_SCOPE, configuration.scope)
        .withFormParam(TadoAuthRequest.P_USERNAME, configuration.username)
        .withFormParam(TadoAuthRequest.P_PASSWORD, configuration.password)
        .withFormParam(TadoAuthRequest.P_GRANT_TYPE, TadoAuthLoginRequest.GRANT_TYPE)
    )

    mock.authServer.allServeEvents.size shouldBe 1
  }

  "refresh with invalid refresh token fails" {
    val REFRESH_TOKEN = "invalidRefreshToken"

    mock.authServer.stubFor(AuthMockMappings.failedRefreshAuthMapping())

    shouldThrow<HttpClientErrorException> {
      authClient.token(TadoAuthRefreshRequest(configuration, REFRESH_TOKEN))
    }

    mock.authServer.verify(
      1,
      postRequestedFor(urlEqualTo(AuthClient.TOKEN_PATH))
        .withHeader(HttpHeaders.CONTENT_TYPE, matching("${MediaType.APPLICATION_FORM_URLENCODED_VALUE}.*"))
        .withFormParam(TadoAuthRequest.P_CLIENT_ID, configuration.clientId)
        .withFormParam(TadoAuthRequest.P_CLIENT_SECRET, configuration.clientSecret)
        .withFormParam(TadoAuthRequest.P_SCOPE, configuration.scope)
        .withFormParam(TadoAuthRequest.P_REFRESH_TOKEN, REFRESH_TOKEN)
        .withFormParam(TadoAuthRequest.P_GRANT_TYPE, TadoAuthRefreshRequest.GRANT_TYPE)
    )

    mock.authServer.allServeEvents.size shouldBe 1
  }
}) {
  @TestConfiguration
  internal class AuthClientIntegrationTestConfig {
    @Bean
    @Primary
    fun authNoRateLimiter(): RateLimiter = object : RateLimiter {
      override fun <T> executeRateLimited(block: () -> T): T {
        return block()
      }
    }
  }
}
