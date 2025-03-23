package click.dobel.tado.exporter.apiclient.auth.model.request

import click.dobel.tado.exporter.apiclient.TadoConfigurationProperties
import com.fasterxml.jackson.annotation.JsonProperty

data class TadoAuthRefreshRequest(
  @JsonProperty(TadoAuthRequest.P_CLIENT_ID)
  override val clientId: String,
  @JsonProperty(TadoAuthRequest.P_REFRESH_TOKEN)
  val refreshToken: String,
) : TadoAuthRequest {

  @JsonProperty(TadoAuthRequest.P_GRANT_TYPE)
  override val grantType: String = GRANT_TYPE

  companion object {
    const val GRANT_TYPE = TadoAuthRequest.P_REFRESH_TOKEN
  }

  constructor(
    configuration: TadoConfigurationProperties,
    refreshToken: String
  ) :
    this(
      configuration.clientId,
      refreshToken
    )

  override fun asMap(): Map<String, String> {
    return super.asMap() + mapOf(TadoAuthRequest.P_REFRESH_TOKEN to refreshToken)
  }
}
