package click.dobel.tado.client

import io.kotlintest.Spec
import io.kotlintest.matchers.collections.shouldHaveAtLeastSize
import io.kotlintest.matchers.numerics.shouldNotBeLessThan
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.StringSpec
import io.micronaut.test.annotation.MicronautTest

@MicronautTest
internal class TadoApiClientTest(
  private val tadoClient: TadoApiClient,
  private val tadoConfiguration: TadoConfiguration
) : StringSpec({
  "/me succeeds" {
    val me = tadoClient.me().blockingGet()
    me.username shouldBe tadoConfiguration.username
  }

  "/zones succeeds" {
    val zones = tadoClient.zones(homeId).blockingGet()
    zones shouldHaveAtLeastSize 1
  }

  "/weather succeeds" {
    val weather = tadoClient.weather(homeId).blockingGet()
    weather.weatherState.value shouldNotBe null
  }
}) {
  companion object {
    var homeId = 0
  }

  override fun beforeSpec(spec: Spec) {
    super.beforeSpec(spec)
    homeId = tadoClient.me().blockingGet().homes[0].id
    homeId shouldNotBeLessThan 0
  }
}
