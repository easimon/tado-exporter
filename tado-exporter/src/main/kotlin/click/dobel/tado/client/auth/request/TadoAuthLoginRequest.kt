package click.dobel.tado.client.auth.request

import click.dobel.tado.client.TadoConfiguration
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected

@Introspected
data class TadoAuthLoginRequest(
  @JsonProperty(TadoAuthRequest.P_CLIENT_ID)
  override val client_id: String,
  @JsonProperty(TadoAuthRequest.P_CLIENT_SECRET)
  override val client_secret: String,
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
  override val grant_type = GRANT_TYPE

  constructor(tadoConfiguration: TadoConfiguration) :
    this(
      tadoConfiguration.clientId,
      tadoConfiguration.clientSecret,
      tadoConfiguration.scope,
      tadoConfiguration.username,
      tadoConfiguration.password
    )
}

