package click.dobel.tado.client.auth.request

import click.dobel.tado.client.TadoConfiguration
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected

@Introspected
data class TadoAuthRefreshRequest(
  @JsonProperty(TadoAuthRequest.P_CLIENT_ID)
  override val client_id: String,
  @JsonProperty(TadoAuthRequest.P_CLIENT_SECRET)
  override val client_secret: String,
  @JsonProperty(TadoAuthRequest.P_SCOPE)
  override val scope: String,
  @JsonProperty(TadoAuthRequest.P_REFRESH_TOKEN)
  val refresh_token: String
) : TadoAuthRequest {
  companion object {
    const val GRANT_TYPE = TadoAuthRequest.P_REFRESH_TOKEN
  }

  @JsonProperty(TadoAuthRequest.P_GRANT_TYPE)
  override val grant_type = GRANT_TYPE

  constructor(tadoConfiguration: TadoConfiguration, refreshToken: String) :
    this(
      tadoConfiguration.clientId,
      tadoConfiguration.clientSecret,
      tadoConfiguration.scope,
      refreshToken
    )
}
