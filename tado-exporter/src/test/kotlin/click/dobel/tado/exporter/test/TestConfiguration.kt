package click.dobel.tado.exporter.test

import click.dobel.tado.exporter.apiclient.TadoConfigurationProperties
import click.dobel.tado.exporter.apiclient.TadoDebuggingConfig
import io.kotest.core.names.TestName
import io.kotest.core.test.TestCase
import java.time.Duration
import java.time.temporal.ChronoUnit

internal object TestConfiguration {

  fun create(test: TestCase) = TadoConfigurationProperties(
    clientId = "test-client-id",
    authServer = "http://localhost:18080",
    apiServer = "http://localhost:18081",
    zoneDiscoveryInterval = Duration.of(5, ChronoUnit.MINUTES),
    authCachePath = "/tmp/tado-exporter-test/${test.name.toDirectoryName()}/auth",
    debug = TadoDebuggingConfig(http = true)
  )

  fun noCache(test: TestCase) = create(test).copy(
    authCachePath = "/dev/null",
  )

  // TODO test on random port
  private val random: () -> Double = Math::random
  private fun randomPort(min: Int, max: Int): Int {
    return (min + random() * (max - min)).toInt()
  }
}

private fun TestName.toDirectoryName() = this.testName.replace(Regex("[^a-zA-Z0-9]"), "_")
