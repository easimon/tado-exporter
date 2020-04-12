package click.dobel.tado.client.auth

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client(AuthClient.SERVICE_ID)
interface AuthClient {

  companion object {
    internal const val SERVICE_ID = "tado-auth"
    const val TOKEN_PATH = "/oauth/token"
  }

  @Post(TOKEN_PATH, produces = [MediaType.APPLICATION_FORM_URLENCODED])
  fun token(@Body auth: TadoAuthRequest): TadoAuthResponse
}
