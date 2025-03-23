package click.dobel.tado.exporter.apiclient.auth

import click.dobel.tado.exporter.apiclient.DEFAULT_DEVICE_CODE
import click.dobel.tado.exporter.apiclient.TadoConfigurationProperties
import click.dobel.tado.exporter.apiclient.auth.thread.TransientPollingError
import click.dobel.tado.exporter.apiclient.testDeviceCodeResponseNoDelay
import click.dobel.tado.exporter.apiclient.testTokenResponse
import click.dobel.tado.exporter.test.TestConfiguration
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import kotlin.time.Duration.Companion.milliseconds

class TadoAuthenticatorTest : FreeSpec({

  lateinit var configuration: TadoConfigurationProperties
  val authenticationClient = mockk<TadoAuthenticationClient>()

  lateinit var state: TadoAuthenticator

  beforeEach {
    configuration = TestConfiguration.noCache(it)
    state = TadoAuthenticator(configuration, authenticationClient)

    every {
      authenticationClient.deviceAuthorization()
    } returns testDeviceCodeResponseNoDelay

    every {
      authenticationClient.token(DEFAULT_DEVICE_CODE)
    } returns testTokenResponse
  }

  afterEach {
    clearMocks(authenticationClient)
  }

  "should immediately start to poll for device code and succeed to authenticate" {
    val currentState = state.currentState

    currentState.shouldBeInstanceOf<TadoAuthenticationState.Authenticated>()
    currentState.accessToken.value shouldBe testTokenResponse.accessToken
    currentState.refreshToken.value shouldBe testTokenResponse.refreshToken

    verifySequence {
      authenticationClient.deviceAuthorization()
      authenticationClient.token(DEFAULT_DEVICE_CODE)
    }
    confirmVerified(authenticationClient)
  }

  "should immediately start to poll for device code and succeed to authenticate after second attempt" {
    every {
      authenticationClient.token(DEFAULT_DEVICE_CODE)
    } throws TransientPollingError("Test") andThen testTokenResponse

    val firstState = state.currentState
    firstState.shouldBeInstanceOf<TadoAuthenticationState.PollingForDeviceCodeToken>()

    eventually(duration = 100.milliseconds) {
      val currentState = state.currentState
      currentState.shouldBeInstanceOf<TadoAuthenticationState.Authenticated>()
      currentState.accessToken.value shouldBe testTokenResponse.accessToken
      currentState.refreshToken.value shouldBe testTokenResponse.refreshToken
    }

    verifySequence {
      authenticationClient.deviceAuthorization()
      authenticationClient.token(DEFAULT_DEVICE_CODE)
      authenticationClient.token(DEFAULT_DEVICE_CODE)
    }

    confirmVerified(authenticationClient)
  }
})
