package click.dobel.tado.client

import click.dobel.tado.test.ApiMockMappings
import click.dobel.tado.test.AuthMockMappings
import click.dobel.tado.test.WireMockSupport
import click.dobel.tado.test.apiPath
import click.dobel.tado.test.withBearerAuth
import com.github.tomakehurst.wiremock.client.WireMock
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.micronaut.http.HttpHeaders
import io.micronaut.http.MediaType
import io.micronaut.test.annotation.MicronautTest

@MicronautTest
internal class TadoApiClientIntegrationTest(
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
        .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON))
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
        .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON))
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

  override fun beforeTest(testCase: TestCase) {
    mock.apiServer.start()
    mock.authServer.start()
    mock.authServer.stubFor(AuthMockMappings.successfulUsernamePasswordAuthMapping())
  }

  override fun afterTest(testCase: TestCase, result: TestResult) {
    mock.apiServer.findAllUnmatchedRequests().size shouldBe 0
    mock.apiServer.resetAll()
    mock.apiServer.shutdown()
    mock.authServer.shutdown()
  }
}
