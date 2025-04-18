package click.dobel.tado.exporter.apiclient

import click.dobel.tado.exporter.test.ApiMockMappings
import click.dobel.tado.exporter.test.AuthMockMappings
import click.dobel.tado.exporter.test.IntegrationTest
import click.dobel.tado.exporter.test.WireMockSupport
import click.dobel.tado.exporter.test.apiPath
import click.dobel.tado.exporter.test.withBearerAuth
import com.github.tomakehurst.wiremock.client.WireMock
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

@IntegrationTest
internal class TadoApiClientIT(
  private val tadoClient: TadoApiClient,
  private val mock: WireMockSupport
) : StringSpec({

  "/me succeeds" {
    mock.apiServer.stubFor(ApiMockMappings.successfulMeMapping())

    val me = tadoClient.me()
    me.name shouldBe ApiMockMappings.USER_FULL_NAME
    me.email shouldBe ApiMockMappings.USER_EMAIL
    me.username shouldBe ApiMockMappings.USER_EMAIL

    mock.apiServer.verify(
      1,
      WireMock.getRequestedFor(WireMock.urlEqualTo(apiPath(TadoApiClient.ME_PATH)))
        .withBearerAuth(AuthMockMappings.DEFAULT_ACCESS_TOKEN)
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
        .withBearerAuth(AuthMockMappings.DEFAULT_ACCESS_TOKEN)
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
}) {

  override suspend fun beforeTest(testCase: TestCase) {
    mock.apiServer.start()
    mock.authServer.start()
    mock.authServer.stubFor(AuthMockMappings.successfulUsernamePasswordAuthMapping())
  }

  override suspend fun afterTest(testCase: TestCase, result: TestResult) {
    try {
      mock.apiServer.findAllUnmatchedRequests().size shouldBe 0
    } finally {
      mock.apiServer.resetAll()
      mock.authServer.resetAll()
      mock.apiServer.shutdown()
      mock.authServer.shutdown()
    }
  }
}
