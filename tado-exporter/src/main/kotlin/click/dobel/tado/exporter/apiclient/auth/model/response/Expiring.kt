package click.dobel.tado.exporter.apiclient.auth.model.response

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.Instant

private const val RESERVE_SECONDS = 20L

interface Expiring {
  val creation: Instant
  val expiresInSeconds: Long

  @get:JsonIgnore
  val expiresAt: Instant
    get() = creation
      .plusSeconds(expiresInSeconds)
      .minusSeconds(RESERVE_SECONDS)

  @get:JsonIgnore
  val isExpired: Boolean get() = Instant.now() > expiresAt
}
