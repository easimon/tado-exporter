package click.dobel.tado.client

import click.dobel.tado.api.HomeInfo
import click.dobel.tado.api.User
import click.dobel.tado.api.WeatherReport
import click.dobel.tado.api.Zone
import click.dobel.tado.api.ZoneState
import click.dobel.tado.util.aop.Logged
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import reactor.core.publisher.Mono

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
  }

  @Get(ME_PATH)
  fun me(): Mono<User>

  @Get("${HOMES_PATH}/{homeId}")
  fun homes(homeId: Int): Mono<HomeInfo>

  @Get("${HOMES_PATH}/{homeId}${ZONES_PATH}")
  fun zones(homeId: Int): Mono<List<Zone>>

  @Logged("Retrieving fresh zone state for HomeId {}, ZoneId {}.", ["homeId", "zoneId"])
  @Get("${HOMES_PATH}/{homeId}${ZONES_PATH}/{zoneId}${STATE_PATH}")
  fun zoneState(homeId: Int, zoneId: Int): Mono<ZoneState>

  @Logged("Retrieving fresh weather report for HomeId {}.", ["homeId"])
  @Get("${HOMES_PATH}/{homeId}/${WEATHER_PATH}")
  fun weather(homeId: Int): Mono<WeatherReport>
}
