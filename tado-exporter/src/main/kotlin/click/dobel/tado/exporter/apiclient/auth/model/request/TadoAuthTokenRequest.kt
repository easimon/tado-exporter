package click.dobel.tado.exporter.apiclient.auth.model.request

import click.dobel.tado.exporter.apiclient.TadoConfigurationProperties
import com.fasterxml.jackson.annotation.JsonProperty

data class TadoAuthTokenRequest(
  @JsonProperty(TadoAuthRequest.P_CLIENT_ID)
  override val clientId: String,
  @JsonProperty(TadoAuthRequest.P_DEVICE_CODE)
  val deviceCode: String,
) : TadoAuthRequest {

  @JsonProperty(TadoAuthRequest.P_GRANT_TYPE)
  override val grantType: String = GRANT_TYPE

  companion object {
    const val GRANT_TYPE = "urn:ietf:params:oauth:grant-type:device_code"
  }

  constructor(
    configuration: TadoConfigurationProperties,
    deviceCode: String
  ) : this(
    clientId = configuration.clientId,
    deviceCode = deviceCode
  )

  override fun asMap(): Map<String, String> {
    return super.asMap() + mapOf(TadoAuthRequest.P_DEVICE_CODE to deviceCode)
  }
}

