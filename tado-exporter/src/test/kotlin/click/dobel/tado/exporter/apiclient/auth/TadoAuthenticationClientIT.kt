package click.dobel.tado.exporter.apiclient.auth

import click.dobel.tado.exporter.apiclient.DEFAULT_ACCESS_TOKEN
import click.dobel.tado.exporter.apiclient.DEFAULT_DEVICE_CODE
import click.dobel.tado.exporter.apiclient.DEFAULT_REFRESH_TOKEN
import click.dobel.tado.exporter.apiclient.REFRESHED_ACCESS_TOKEN
import click.dobel.tado.exporter.apiclient.auth.model.refreshtoken.toRefreshToken
import click.dobel.tado.exporter.test.AuthMockMappings
import click.dobel.tado.exporter.test.IntegrationTest
import click.dobel.tado.exporter.test.NoRatelimiterConfiguration
import click.dobel.tado.exporter.test.WireMockSupport
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.springframework.context.annotation.Import

@IntegrationTest
@Import(NoRatelimiterConfiguration::class)
class TadoAuthenticationClientIT(
  tadoAuthenticationClient: TadoAuthenticationClient,
  private val mock: WireMockSupport
) : StringSpec({

  beforeTest {
    mock.authServer.start()
  }

  afterEach {
    try {
      mock.authServer.findAllUnmatchedRequests().shouldBeEmpty()
    } finally {
      mock.authServer.resetAll()
      mock.authServer.shutdown()
    }
  }

  "can fetch device authorization code" {
    mock.authServer.stubFor(AuthMockMappings.successfulDeviceAuthorization())

    val deviceCodeResponse = tadoAuthenticationClient.deviceAuthorization()

    deviceCodeResponse.deviceCode shouldBe DEFAULT_DEVICE_CODE
    mock.authServer.allServeEvents shouldHaveSize 1
  }

  "can fetch access token" {
    mock.authServer.stubFor(AuthMockMappings.successfulDeviceAuthorization())
    mock.authServer.stubFor(AuthMockMappings.successfulDeviceCodeTokenFetch())

    val deviceCodeResponse = tadoAuthenticationClient.deviceAuthorization()
    val authResponse = tadoAuthenticationClient.token(deviceCodeResponse.deviceCode)

    authResponse.accessToken shouldBe DEFAULT_ACCESS_TOKEN
    mock.authServer.allServeEvents shouldHaveSize 2
  }

  "can refresh access token" {
    mock.authServer.stubFor(AuthMockMappings.successfulDeviceAuthorization())
    mock.authServer.stubFor(AuthMockMappings.successfulDeviceCodeTokenFetch())
    mock.authServer.stubFor(AuthMockMappings.successfulRefreshTokenFetch())

    val deviceCodeResponse = tadoAuthenticationClient.deviceAuthorization()
    val authResponse = tadoAuthenticationClient.token(deviceCodeResponse.deviceCode)

    authResponse.refreshToken shouldBe DEFAULT_REFRESH_TOKEN

    val refreshResponse = tadoAuthenticationClient.refresh(authResponse.toRefreshToken())
    refreshResponse.accessToken shouldBe REFRESHED_ACCESS_TOKEN

    mock.authServer.allServeEvents shouldHaveSize 3
  }
})
