package click.dobel.tado.exporter.apiclient.auth.model.request

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

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

  val clientId: String
  val clientSecret: String
  val scope: String
  val grantType: String

  fun asMap(): MultiValueMap<String, String> {
    val result = LinkedMultiValueMap<String, String>()
    mapOf(
      P_CLIENT_ID to clientId,
      P_CLIENT_SECRET to clientSecret,
      P_SCOPE to scope,
      P_GRANT_TYPE to grantType
    ).forEach { (k, v) ->
      result.add(k, v)
    }
    return result
  }

  fun asRequestEntity(): HttpEntity<MultiValueMap<String, String>> {
    val headers = HttpHeaders()
    headers.accept = listOf(MediaType.APPLICATION_JSON)
    headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

    return HttpEntity(asMap(), headers)
  }
}
