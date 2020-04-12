package click.dobel.tado.client.auth

import click.dobel.tado.client.TadoConfiguration
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected

@Introspected
sealed class TadoAuthRequest {

  companion object {
    const val P_CLIENT_ID = "client_id"
    const val P_CLIENT_SECRET = "client_secret"
    const val P_SCOPE = "scope"
    const val P_USERNAME = "username"
    const val P_PASSWORD = "password"
    const val P_REFRESH_TOKEN = "refresh_token"
    const val P_GRANT_TYPE = "grant_type"

    const val P_GRANT_TYPE_PASSWORD = P_PASSWORD
    const val P_GRANT_TYPE_REFRESH_TOKEN = P_REFRESH_TOKEN
  }

  /* TODO: find how to convince micronaut to use snake case when serializing for form request */
  abstract val client_id: String
  abstract val client_secret: String
  abstract val scope: String
  abstract val grant_type: String

  data class TadoAuthLoginRequest(
    @JsonProperty(P_CLIENT_ID)
    override val client_id: String,
    @JsonProperty(P_CLIENT_SECRET)
    override val client_secret: String,
    @JsonProperty(P_SCOPE)
    override val scope: String,
    @JsonProperty(P_USERNAME)
    val username: String,
    @JsonProperty(P_PASSWORD)
    val password: String
  ) : TadoAuthRequest() {
    @JsonProperty(P_GRANT_TYPE)
    override val grant_type = P_GRANT_TYPE_PASSWORD

    constructor(tadoConfiguration: TadoConfiguration) :
      this(
        tadoConfiguration.clientId,
        tadoConfiguration.clientSecret,
        tadoConfiguration.scope,
        tadoConfiguration.username,
        tadoConfiguration.password
      )
  }

  data class TadoAuthRefreshRequest(
    @JsonProperty(P_CLIENT_ID)
    override val client_id: String,
    @JsonProperty(P_CLIENT_SECRET)
    override val client_secret: String,
    @JsonProperty(P_SCOPE)
    override val scope: String,
    @JsonProperty(P_REFRESH_TOKEN)
    val refresh_token: String
  ) : TadoAuthRequest() {
    @JsonProperty(P_GRANT_TYPE)
    override val grant_type = P_GRANT_TYPE_REFRESH_TOKEN

    constructor(tadoConfiguration: TadoConfiguration, refreshToken: String) :
      this(
        tadoConfiguration.clientId,
        tadoConfiguration.clientSecret,
        tadoConfiguration.scope,
        refreshToken
      )
  }
}

