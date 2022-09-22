package click.dobel.tado.exporter.test

import click.dobel.tado.exporter.apiclient.TadoConfigurationProperties
import java.time.Duration
import java.time.temporal.ChronoUnit

internal object TestConfiguration {

  val INSTANCE = TadoConfigurationProperties(
    username = "username",
    password = "password",
    scope = "test.scope",
    clientId = "test-client-id",
    clientSecret = "test-client-secret",
    authServer = "http://localhost:18080",
    apiServer = "http://localhost:18081",
    zoneDiscoveryInterval = Duration.of(5, ChronoUnit.MINUTES)
  )

  // TODO test on random port
  private val random: () -> Double = Math::random
  private fun randomPort(min: Int, max: Int): Int {
    return (min + random() * (max - min)).toInt()
  }
}
