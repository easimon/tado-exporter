package click.dobel.tado.exporter.test

import click.dobel.tado.exporter.apiclient.TadoConfigurationProperties
import click.dobel.tado.exporter.apiclient.TadoDebuggingConfig
import java.time.Duration
import java.time.temporal.ChronoUnit

internal object TestConfiguration {

  val INSTANCE = TadoConfigurationProperties(
    clientId = "test-client-id",
    authServer = "http://localhost:18080",
    apiServer = "http://localhost:18081",
    zoneDiscoveryInterval = Duration.of(5, ChronoUnit.MINUTES),
    authCachePath = "/tmp/tado-exporter/auth",
    debug = TadoDebuggingConfig(http = true)
  )

  // TODO test on random port
  private val random: () -> Double = Math::random
  private fun randomPort(min: Int, max: Int): Int {
    return (min + random() * (max - min)).toInt()
  }
}
