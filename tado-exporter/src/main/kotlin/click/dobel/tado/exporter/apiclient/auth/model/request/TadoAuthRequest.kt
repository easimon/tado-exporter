package click.dobel.tado.exporter.apiclient.auth.model.request

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = TadoAuthRequest.P_GRANT_TYPE,
  visible = true
)
@JsonSubTypes(
  JsonSubTypes.Type(TadoAuthTokenRequest::class, name = TadoAuthTokenRequest.GRANT_TYPE),
  JsonSubTypes.Type(TadoAuthRefreshRequest::class, name = TadoAuthRefreshRequest.GRANT_TYPE)
)
interface TadoAuthRequest : FormUrlEncodedRequest {
  companion object {
    const val P_CLIENT_ID = "client_id"
    const val P_DEVICE_CODE = "device_code"
    const val P_SCOPE = "scope"
    const val P_REFRESH_TOKEN = "refresh_token"
    const val P_GRANT_TYPE = "grant_type"
  }

  val clientId: String
  val grantType: String

  override fun asMap(): Map<String, String> = mapOf(
    P_CLIENT_ID to clientId,
    P_GRANT_TYPE to grantType
  )
}

fun Map<String, String>.asMultiValueMap(): MultiValueMap<String, String> {
  val result = LinkedMultiValueMap<String, String>()
  forEach { (key, value) -> result.add(key, value) }
  return result
}
