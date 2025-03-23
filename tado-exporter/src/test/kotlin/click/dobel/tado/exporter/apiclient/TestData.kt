package click.dobel.tado.exporter.apiclient

import click.dobel.tado.exporter.apiclient.auth.model.response.TadoAuthResponse
import click.dobel.tado.exporter.apiclient.auth.model.response.TadoDeviceAuthResponse


const val DEFAULT_ACCESS_TOKEN = "accessToken"
const val REFRESHED_ACCESS_TOKEN = "refreshedAccessToken"
const val DEFAULT_DEVICE_CODE = "fake-device-code"
const val DEFAULT_TOKEN_TYPE = "bearer"
const val DEFAULT_REFRESH_TOKEN = "refreshToken"
const val DEFAULT_EXPIRES_IN_SECONDS = 1337L
const val DEFAULT_POLLING_SECONDS = 5L
const val DEFAULT_SCOPE = "test.scope"
const val DEFAULT_USER_ID = "user-id@domain.com"

val testDeviceCodeResponse
  get() = TadoDeviceAuthResponse(
    deviceCode = DEFAULT_DEVICE_CODE,
    expiresInSeconds = DEFAULT_EXPIRES_IN_SECONDS,
    intervalSeconds = DEFAULT_POLLING_SECONDS,
    userCode = DEFAULT_DEVICE_CODE,
    verificationUri = "https://example.invalid",
    verificationUriComplete = "https://example.invalid/?code=$DEFAULT_DEVICE_CODE"
  )

val testDeviceCodeResponseNoDelay
  get() = testDeviceCodeResponse
    .copy(intervalSeconds = 0)

val testTokenResponse
  get() = TadoAuthResponse(
    DEFAULT_ACCESS_TOKEN, DEFAULT_EXPIRES_IN_SECONDS,
    DEFAULT_REFRESH_TOKEN,
    DEFAULT_SCOPE,
    DEFAULT_TOKEN_TYPE, DEFAULT_USER_ID
  )
