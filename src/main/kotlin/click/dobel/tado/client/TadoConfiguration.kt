package click.dobel.tado.client

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Requires

@ConfigurationProperties(TadoConfiguration.Prefix)
@Requires(TadoConfiguration.Prefix)
class TadoConfiguration {

  companion object {
    const val Prefix = "tado"
  }

  lateinit var username: String
  lateinit var password: String
  lateinit var clientId: String
  lateinit var clientSecret: String
  lateinit var scope: String
  lateinit var apiServer: String
  lateinit var authServer: String
}
