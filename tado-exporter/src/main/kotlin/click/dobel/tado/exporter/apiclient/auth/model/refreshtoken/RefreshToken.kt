package click.dobel.tado.exporter.apiclient.auth.model.refreshtoken

import click.dobel.tado.exporter.apiclient.auth.model.refreshtoken.RefreshToken.Companion.VALIDITY_DURATION
import click.dobel.tado.exporter.apiclient.auth.model.response.Expiring
import click.dobel.tado.exporter.apiclient.auth.model.response.TadoAuthResponse
import java.time.Instant
import kotlin.time.Duration.Companion.days

data class RefreshToken(
  val value: String,
  override val creation: Instant,
  override val expiresInSeconds: Long,
) : Expiring, Comparable<RefreshToken> {

  companion object {
    // according to documentation, not in token
    val VALIDITY_DURATION = 30.days.inWholeSeconds
  }

  override fun compareTo(other: RefreshToken): Int {
    return expiresAt.compareTo(other.expiresAt)
  }
}

fun TadoAuthResponse.toRefreshToken(): RefreshToken = RefreshToken(
  refreshToken,
  creation,
  VALIDITY_DURATION
)
