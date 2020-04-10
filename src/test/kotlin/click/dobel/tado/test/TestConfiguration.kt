package click.dobel.tado.test

import click.dobel.tado.client.TadoConfiguration

internal object TestConfiguration {

  val INSTANCE = TadoConfiguration()

  init {
    INSTANCE.username = "username"
    INSTANCE.password = "password"
    INSTANCE.scope = "test.scope"
    INSTANCE.clientId = "test-client-id"
    INSTANCE.clientSecret = "test-client-secret"
    INSTANCE.authServer = "http://localhost:18080"
    INSTANCE.apiServer = "http://localhost:18081"
  }

  // TODO test on random port
  private val random: () -> Double = Math::random
  private fun randomPort(min: Int, max: Int): Int {
    return (min + random() * (max - min)).toInt()
  }
}
