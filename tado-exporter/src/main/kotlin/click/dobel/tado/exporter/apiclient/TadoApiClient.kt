package click.dobel.tado.exporter.apiclient

import click.dobel.tado.api.HomeInfo
import click.dobel.tado.api.HomeState
import click.dobel.tado.api.User
import click.dobel.tado.api.WeatherReport
import click.dobel.tado.api.ZoneState
import click.dobel.tado.exporter.apiclient.auth.TadoAuthFilter
import click.dobel.tado.exporter.apiclient.model.Zones
import click.dobel.tado.util.aop.Logged
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class TadoApiClient(
  private val configuration: TadoConfigurationProperties,
  authFilter: TadoAuthFilter,
  restTemplateBuilder: RestTemplateBuilder
) {

  companion object {
    const val BASE_URL = "/api/v2"

    const val ME_PATH = "/me"
    const val HOMES_PATH = "/homes"
    const val ZONES_PATH = "/zones"
    const val STATE_PATH = "/state"
    const val WEATHER_PATH = "/weather"
  }

  @Cacheable("User", sync = true)
  fun me() = get<User>(ME_PATH)

  @Cacheable("HomeInfo", sync = true)
  fun homes(homeId: Int): HomeInfo =
    get("$HOMES_PATH/${homeId}")

  @Cacheable("HomeState", sync = true)
  fun homeState(homeId: Int): HomeState =
    get("$HOMES_PATH/${homeId}/state")

  @Cacheable("Zone", sync = true)
  fun zones(homeId: Int): Zones =
    get("$HOMES_PATH/${homeId}/$ZONES_PATH")

  @Cacheable("ZoneState", sync = true)
  @Logged("Retrieving fresh zone state for HomeId {}, ZoneId {}.", ["homeId", "zoneId"])
  fun zoneState(homeId: Int, zoneId: Int): ZoneState =
    get("$HOMES_PATH/${homeId}/$ZONES_PATH/${zoneId}/$STATE_PATH")

  @Cacheable("WeatherReport", sync = true)
  @Logged("Retrieving fresh weather report for HomeId {}.", ["homeId"])
  fun weather(homeId: Int): WeatherReport =
    get("$HOMES_PATH/${homeId}/$WEATHER_PATH")

  private inline fun <reified T : Any> get(
    path: String
  ): T {
    return restTemplate.getForObject(url(path))
  }

  private fun url(
    path: String
  ): String = "${configuration.apiServer}$BASE_URL${slash(path)}"

  private fun slash(
    path: String
  ): String = if (path.startsWith("/")) path else "/$path"

  private val restTemplate: RestTemplate = restTemplateBuilder
    .additionalInterceptors(authFilter)
    .build()
}

