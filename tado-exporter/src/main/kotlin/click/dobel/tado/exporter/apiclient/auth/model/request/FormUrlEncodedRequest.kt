package click.dobel.tado.exporter.apiclient.auth.model.request

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.MultiValueMap

interface FormUrlEncodedRequest {
  fun asMap(): Map<String, String>

  fun asMultiValueMap(): MultiValueMap<String, String> = asMap().asMultiValueMap()

  fun asRequestEntity(): HttpEntity<MultiValueMap<String, String>> {
    val headers = HttpHeaders()
    headers.accept = listOf(MediaType.APPLICATION_JSON)
    headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

    return HttpEntity(asMap().asMultiValueMap(), headers)
  }
}
