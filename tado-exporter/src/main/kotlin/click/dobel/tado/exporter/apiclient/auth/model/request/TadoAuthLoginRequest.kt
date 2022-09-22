package click.dobel.tado.exporter.apiclient.auth.model.request

import click.dobel.tado.exporter.apiclient.TadoConfigurationProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.util.MultiValueMap

data class TadoAuthLoginRequest(
  @JsonProperty(TadoAuthRequest.P_CLIENT_ID)
  override val clientId: String,
  @JsonProperty(TadoAuthRequest.P_CLIENT_SECRET)
  override val clientSecret: String,
  @JsonProperty(TadoAuthRequest.P_SCOPE)
  override val scope: String,
  @JsonProperty(TadoAuthRequest.P_USERNAME)
  val username: String,
  @JsonProperty(TadoAuthRequest.P_PASSWORD)
  val password: String
) : TadoAuthRequest {
  companion object {
    const val GRANT_TYPE = TadoAuthRequest.P_PASSWORD
  }

  @JsonProperty(TadoAuthRequest.P_GRANT_TYPE)
  override val grantType = GRANT_TYPE

  constructor(tadoConfigurationProperties: TadoConfigurationProperties) :
    this(
      tadoConfigurationProperties.clientId,
      tadoConfigurationProperties.clientSecret,
      tadoConfigurationProperties.scope,
      tadoConfigurationProperties.username,
      tadoConfigurationProperties.password
    )

  override fun asMap(): MultiValueMap<String, String> {
    val result = super.asMap()
    result.add(TadoAuthRequest.P_USERNAME, username)
    result.add(TadoAuthRequest.P_PASSWORD, password)
    return result
  }
}

