package click.dobel.tado.exporter.apiclient

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.time.Duration

@ConstructorBinding
@ConfigurationProperties("tado")
class TadoConfigurationProperties(
  val username: String,
  val password: String,
  val clientId: String,
  val clientSecret: String,
  val scope: String,
  val apiServer: String,
  val authServer: String,
  val zoneDiscoveryInterval: Duration
)
