package click.dobel.tado.client

import click.dobel.tado.api.HomeInfo
import click.dobel.tado.api.User
import click.dobel.tado.api.WeatherReport
import click.dobel.tado.api.Zone
import click.dobel.tado.api.ZoneState
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client

@Client(TadoApiClient.SERVICE_ID, path = TadoApiClient.BASE_URL)
interface TadoApiClient {

  companion object {
    internal const val SERVICE_ID = "tado-api"
    const val BASE_URL = "/api/v2"

    const val ME_PATH = "/me"
    const val HOMES_PATH = "/homes"
    const val ZONES_PATH = "/zones"
    const val STATE_PATH = "/state"
    const val WEATHER_PATH = "/weather"

    private const val CACHE_NAME_ZONESTATE = "tado-zonestate"
    private const val CACHE_NAME_WEATHER = "tado-weather"
  }

  @Get(ME_PATH)
  fun me(): User

  @Get("${HOMES_PATH}/{homeId}")
  fun homes(homeId: Int): HomeInfo

  @Get("${HOMES_PATH}/{homeId}${ZONES_PATH}")
  fun zones(homeId: Int): List<Zone>

  /* calls to these methods are triggered by micrometer when refreshing all metered values.
   * since one call contains the data for multiple gauges,
   * cache it for "micrometer refresh interval minus a few secs" */
  @Cacheable(cacheNames = [CACHE_NAME_ZONESTATE], atomic = true)
  @Get("${HOMES_PATH}/{homeId}${ZONES_PATH}/{zoneId}${STATE_PATH}")
  fun zoneState(homeId: Int, zoneId: Int): ZoneState

  @Cacheable(cacheNames = [CACHE_NAME_WEATHER], atomic = true)
  @Get("${HOMES_PATH}/{homeId}/${WEATHER_PATH}")
  fun weather(homeId: Int): WeatherReport
}
