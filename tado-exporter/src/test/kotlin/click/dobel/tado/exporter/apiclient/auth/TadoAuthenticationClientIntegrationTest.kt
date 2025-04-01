package click.dobel.tado.exporter.apiclient.auth

import click.dobel.tado.exporter.apiclient.auth.TadoAuthenticationClientIntegrationTest.AuthClientIntegrationTestConfig
import click.dobel.tado.exporter.ratelimit.RateLimiter
import click.dobel.tado.exporter.test.IntegrationTest
import io.kotest.core.spec.style.StringSpec
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary

@IntegrationTest
@Import(AuthClientIntegrationTestConfig::class)
class TadoAuthenticationClientIntegrationTest(
  tadoAuthenticationClient: TadoAuthenticationClient,
) : StringSpec({

  "authenticates correctly" {
    val result = tadoAuthenticationClient.deviceAuthorization()

    println(result)
  }

}) {
  @TestConfiguration
  internal class AuthClientIntegrationTestConfig {
    @Bean
    @Primary
    fun authNoRateLimiter(): RateLimiter = object : RateLimiter {
      override fun <T> executeRateLimited(block: () -> T): T {
        return block()
      }
    }
  }
}
