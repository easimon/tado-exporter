package click.dobel.tado.client

//@MicronautTest
//internal class TadoApiClientTest(
//  private val tadoClient: TadoApiClient,
//  private val tadoConfiguration: TadoConfiguration
//) : StringSpec({
//
//  "/me succeeds" {
//    val me = tadoClient.me()
//    me.username shouldBe tadoConfiguration.username
//  }
//  "/zones succeeds" {
//    val zones = tadoClient.zones(homeId)
//    zones shouldHaveAtLeastSize 1
//  }
//
//  "/weather succeeds" {
//    val weather = tadoClient.weather(homeId)
//    weather.weatherState.value shouldNotBe null
//  }
//}) {
//  companion object {
//    var homeId = 0
//  }
//
//  override fun beforeSpec(spec: Spec) {
//    homeId = tadoClient.me().homes[0].id
//    homeId shouldNotBeLessThan 0
//  }
//}
