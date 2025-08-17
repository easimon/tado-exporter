package click.dobel.tado.exporter.apiclient

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("tado")
data class TadoConfigurationProperties(
  val clientId: String,
  val apiServer: String,
  val authServer: String,
  val authCachePath: String,
  val zoneDiscoveryInterval: JavaDuration,
  val debug: TadoDebuggingConfig = TadoDebuggingConfig(),
)

data class TadoDebuggingConfig(
  val http: Boolean = false
)

typealias JavaDuration = java.time.Duration
