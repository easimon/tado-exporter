package click.dobel.tado.client.auth

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client("https://auth.tado.com")
interface AuthClient {

  @Post("/oauth/token", produces = [MediaType.APPLICATION_FORM_URLENCODED])
  fun token(@Body auth: TadoAuthRequest): TadoAuthResponse
}
