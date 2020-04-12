package click.dobel.tado.client.auth.request

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.micronaut.core.annotation.Introspected

@Introspected
@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = TadoAuthRequest.P_GRANT_TYPE,
  visible = true
)
@JsonSubTypes(
  JsonSubTypes.Type(TadoAuthLoginRequest::class, name = TadoAuthLoginRequest.GRANT_TYPE),
  JsonSubTypes.Type(TadoAuthRefreshRequest::class, name = TadoAuthRefreshRequest.GRANT_TYPE)
)
interface TadoAuthRequest {
  companion object {
    const val P_CLIENT_ID = "client_id"
    const val P_CLIENT_SECRET = "client_secret"
    const val P_SCOPE = "scope"
    const val P_USERNAME = "username"
    const val P_PASSWORD = "password"
    const val P_REFRESH_TOKEN = "refresh_token"
    const val P_GRANT_TYPE = "grant_type"
  }

  /* TODO: find how to convince micronaut to use snake case when serializing for form request */
  val client_id: String
  val client_secret: String
  val scope: String
  val grant_type: String
}
