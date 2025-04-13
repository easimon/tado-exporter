package click.dobel.tado.exporter.apiclient.auth.model.accesstoken

import click.dobel.tado.exporter.apiclient.auth.model.response.Expiring
import click.dobel.tado.exporter.apiclient.auth.model.response.TadoAuthResponse
import java.time.Instant

sealed class AccessToken {
  data object None : AccessToken()
  data class BearerToken(
    val value: String,
    override val expiresInSeconds: Long,
    override val creation: Instant
  ) : AccessToken(), Expiring
}

fun TadoAuthResponse.toAccessToken(): AccessToken.BearerToken = AccessToken.BearerToken(
  accessToken,
  expiresInSeconds,
  creation
)
