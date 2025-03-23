package click.dobel.tado.exporter.apiclient.auth.model.request

import click.dobel.tado.exporter.apiclient.TadoConfigurationProperties
import com.fasterxml.jackson.annotation.JsonProperty

data class TadoDeviceAuthRequest(
  @JsonProperty(TadoAuthRequest.P_CLIENT_ID)
  val clientId: String,
) : FormUrlEncodedRequest {

  @JsonProperty(TadoAuthRequest.P_SCOPE)
  val scope: String = "offline_access"

  constructor(configuration: TadoConfigurationProperties) : this(
    configuration.clientId,
  )

  override fun asMap(): Map<String, String> {
    return mapOf(
      TadoAuthRequest.P_CLIENT_ID to clientId,
      TadoAuthRequest.P_SCOPE to scope
    )
  }
}
