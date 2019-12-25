package click.dobel.tado.client.auth

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class TadoAuthResponse(

  @JsonProperty("access_token")
  val accessToken: String,
  @JsonProperty("token_type")
  val tokenType: String,
  @JsonProperty("refresh_token")
  val refreshToken: String,
  @JsonProperty("expires_in")
  val expiresIn: Long,
  @JsonProperty("scope")
  val scope: String,
  @JsonProperty("jti")
  val jti: String,

  @JsonIgnore
  val expiresAt: Instant = Instant.now().plusSeconds(expiresIn - EXPIRY_RESERVE)
) {
  companion object {
    const val EXPIRY_RESERVE = 50
  }

  fun isExpired() = Instant.now() > expiresAt
}
