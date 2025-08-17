package click.dobel.tado.exporter.test

import click.dobel.tado.exporter.TadoExporterApplication
import click.dobel.tado.exporter.ratelimit.NoRateLimiter
import click.dobel.tado.exporter.ratelimit.RateLimiterFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles
import java.lang.annotation.Inherited

@Inherited
@SpringBootTest(classes = [TadoExporterApplication::class])
@ActiveProfiles("test")
annotation class IntegrationTest

internal class NoRatelimiterConfiguration {
  @Bean
  @Primary
  fun noRateLimiterFactory(): RateLimiterFactory = ::NoRateLimiter
}
