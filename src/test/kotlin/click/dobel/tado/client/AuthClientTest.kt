package click.dobel.tado.client

import click.dobel.tado.client.auth.AuthClient
import click.dobel.tado.client.auth.TadoAuthRequest
import io.kotlintest.matchers.string.beEmpty
import io.kotlintest.shouldNot
import io.kotlintest.specs.StringSpec
import io.micronaut.test.annotation.MicronautTest

@MicronautTest
internal class AuthClientTest(
  private val authClient: AuthClient,
  private val configuration: TadoConfiguration
) : StringSpec({
  "authenticates" {
    val result = authClient.token(TadoAuthRequest.TadoAuthLoginRequest(configuration))
    result.accessToken shouldNot beEmpty()
  }
})
