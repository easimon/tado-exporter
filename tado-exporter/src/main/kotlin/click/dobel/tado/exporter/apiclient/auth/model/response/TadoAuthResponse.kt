package click.dobel.tado.exporter.apiclient.auth.model.response

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class TadoAuthResponse(
  @JsonProperty("access_token")
  val accessToken: String,
  @JsonProperty("expires_in")
  override val expiresInSeconds: Long,
  @JsonProperty("refresh_token")
  val refreshToken: String,
  @JsonProperty("scope")
  val scope: String,
  @JsonProperty("token_type")
  val tokenType: String,
  @JsonProperty("userId")
  val userId: String,
  @JsonIgnore
  override val creation: Instant = Instant.now()
) : Expiring {
  companion object {
    fun fromPersistedRefreshToken(refreshToken: String): TadoAuthResponse {
      return TadoAuthResponse(
        accessToken = "invalid",
        expiresInSeconds = -1L,
        refreshToken = refreshToken,
        scope = "offline_access",
        tokenType = "invalid",
        userId = "unknown"
      )
    }
  }
}
