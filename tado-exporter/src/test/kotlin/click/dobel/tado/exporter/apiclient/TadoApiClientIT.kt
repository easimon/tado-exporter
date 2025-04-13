package click.dobel.tado.exporter.apiclient

import click.dobel.tado.exporter.apiclient.auth.TadoAuthenticator
import click.dobel.tado.exporter.apiclient.auth.model.accesstoken.AccessToken
import click.dobel.tado.exporter.test.ApiMockMappings
import click.dobel.tado.exporter.test.ApiMockMappings.apiPath
import click.dobel.tado.exporter.test.AuthMockMappings
import click.dobel.tado.exporter.test.IntegrationTest
import click.dobel.tado.exporter.test.WireMockSupport
import click.dobel.tado.exporter.test.withBearerAuth
import com.github.tomakehurst.wiremock.client.WireMock
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beOfType
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import kotlin.time.Duration.Companion.seconds

@IntegrationTest
@TestPropertySource(
  properties = [
    "tado.auth-cache-path=/dev/null",
  ]
)
internal class TadoApiClientIT(
  private val tadoClient: TadoApiClient,
  private val tadoAuthenticationState: TadoAuthenticator,
  private val mock: WireMockSupport
) : StringSpec({

  beforeEach {
    mock.apiServer.start()
    mock.authServer.start()
    mock.authServer.stubFor(AuthMockMappings.successfulDeviceAuthorization())
    mock.authServer.stubFor(AuthMockMappings.successfulDeviceCodeTokenFetch())
    // wait for token obtain
    eventually(1.seconds) {
      tadoAuthenticationState.accessToken should beOfType<AccessToken.BearerToken>()
    }
  }

  afterEach {
    try {
      mock.apiServer.findAllUnmatchedRequests().size shouldBe 0
    } finally {
      mock.apiServer.resetAll()
      mock.authServer.resetAll()
      mock.apiServer.shutdown()
      mock.authServer.shutdown()
    }
  }

  "/me succeeds" {
    mock.apiServer.stubFor(ApiMockMappings.successfulMeMapping())

    val me = tadoClient.me()
    me.name shouldBe ApiMockMappings.USER_FULL_NAME
    me.email shouldBe ApiMockMappings.USER_EMAIL
    me.username shouldBe ApiMockMappings.USER_EMAIL

    mock.apiServer.verify(
      1,
      WireMock.getRequestedFor(WireMock.urlEqualTo(apiPath(TadoApiClient.ME_PATH)))
        .withBearerAuth(DEFAULT_ACCESS_TOKEN)
        .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
    )

    mock.apiServer.allServeEvents.size shouldBe 1
  }

  "/homes succeeds" {
    mock.apiServer.stubFor(ApiMockMappings.successfulHomesMapping())

    val homes = tadoClient.homes(ApiMockMappings.HOME_ID)

    homes.id shouldBe ApiMockMappings.HOME_ID
    homes.name shouldBe "Home"

    mock.apiServer.verify(
      1,
      WireMock.getRequestedFor(WireMock.urlEqualTo(apiPath(TadoApiClient.HOMES_PATH + "/" + ApiMockMappings.HOME_ID)))
        .withBearerAuth(DEFAULT_ACCESS_TOKEN)
        .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
    )

    mock.apiServer.allServeEvents.size shouldBe 1
  }
  // TODO: other methods
  //
  //  "/zones succeeds" {
  //    val zones = tadoClient.zones(homeId)
  //    zones shouldHaveAtLeastSize 1
  //  }
  //
  //  "/weather succeeds" {
  //    val weather = tadoClient.weather(homeId)
  //    weather.weatherState.value shouldNotBe null
  //  }
})
