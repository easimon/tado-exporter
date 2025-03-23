package click.dobel.tado.exporter.apiclient.auth.model.response

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class TadoDeviceAuthResponse(
  @JsonProperty("device_code")
  val deviceCode: String,
  @JsonProperty("expires_in")
  override val expiresInSeconds: Long,
  @JsonProperty("interval")
  val intervalSeconds: Long,
  @JsonProperty("user_code")
  val userCode: String,
  @JsonProperty("verification_uri")
  val verificationUri: String,
  @JsonProperty("verification_uri_complete")
  val verificationUriComplete: String,
  @JsonIgnore
  override val creation: Instant = Instant.now()
) : Expiring {

  @JsonIgnore
  val pollingInterval: Duration = intervalSeconds.seconds
}
