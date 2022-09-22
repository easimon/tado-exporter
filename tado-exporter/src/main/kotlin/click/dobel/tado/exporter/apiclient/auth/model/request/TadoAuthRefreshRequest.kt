package click.dobel.tado.exporter.apiclient.auth.model.request

import click.dobel.tado.exporter.apiclient.TadoConfigurationProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.util.MultiValueMap

data class TadoAuthRefreshRequest(
  @JsonProperty(TadoAuthRequest.P_CLIENT_ID)
  override val clientId: String,
  @JsonProperty(TadoAuthRequest.P_CLIENT_SECRET)
  override val clientSecret: String,
  @JsonProperty(TadoAuthRequest.P_SCOPE)
  override val scope: String,
  @JsonProperty(TadoAuthRequest.P_REFRESH_TOKEN)
  val refresh_token: String
) : TadoAuthRequest {
  companion object {
    const val GRANT_TYPE = TadoAuthRequest.P_REFRESH_TOKEN
  }

  @JsonProperty(TadoAuthRequest.P_GRANT_TYPE)
  override val grantType = GRANT_TYPE

  constructor(tadoConfigurationProperties: TadoConfigurationProperties, refreshToken: String) :
    this(
      tadoConfigurationProperties.clientId,
      tadoConfigurationProperties.clientSecret,
      tadoConfigurationProperties.scope,
      refreshToken
    )

  override fun asMap(): MultiValueMap<String, String> {
    val result = super.asMap()
    result.add(TadoAuthRequest.P_REFRESH_TOKEN, refresh_token)
    return result
  }
}
