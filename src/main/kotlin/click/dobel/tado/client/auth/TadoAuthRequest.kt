package click.dobel.tado.client.auth

import click.dobel.tado.client.TadoConfiguration
import com.fasterxml.jackson.annotation.JsonProperty

sealed class TadoAuthRequest {

  /* TODO: find how to convince micronaut to use snake case when serializing for form request */
  abstract val client_id: String
  abstract val client_secret: String
  abstract val scope: String
  abstract val grant_type: String

  data class TadoAuthLoginRequest(
    @JsonProperty("client_id")
    override val client_id: String,
    @JsonProperty("client_secret")
    override val client_secret: String,
    @JsonProperty("scope")
    override val scope: String,
    @JsonProperty("username")
    val username: String,
    @JsonProperty("password")
    val password: String
  ) : TadoAuthRequest() {
    @JsonProperty("grant_type")
    override val grant_type = "password"

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
    @JsonProperty("client_id")
    override val client_id: String,
    @JsonProperty("client_secret")
    override val client_secret: String,
    @JsonProperty("scope")
    override val scope: String,
    @JsonProperty("refresh_token")
    val refresh_token: String
  ) : TadoAuthRequest() {
    @JsonProperty("grant_type")
    override val grant_type = "refresh_token"

    constructor(tadoConfiguration: TadoConfiguration, refreshToken: String) :
      this(
        tadoConfiguration.clientId,
        tadoConfiguration.clientSecret,
        tadoConfiguration.scope,
        refreshToken
      )

    constructor(tadoConfiguration: TadoConfiguration, tadoAuthResponse: TadoAuthResponse) :
      this(
        tadoConfiguration,
        tadoAuthResponse.refreshToken
      )
  }
}

