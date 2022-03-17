package click.dobel.tado.client.auth

import click.dobel.tado.client.auth.request.TadoAuthRequest
import click.dobel.tado.client.auth.response.TadoAuthResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import reactor.core.publisher.Mono

@ExecuteOn(TaskExecutors.IO)
@Client(AuthClient.SERVICE_ID)
interface AuthClient {

  companion object {
    internal const val SERVICE_ID = "tado-auth"
    const val TOKEN_PATH = "/oauth/token"
  }

  @Post(TOKEN_PATH, produces = [MediaType.APPLICATION_FORM_URLENCODED])
  fun token(@Body auth: TadoAuthRequest): Mono<TadoAuthResponse>
}
