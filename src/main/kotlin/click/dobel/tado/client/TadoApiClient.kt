package click.dobel.tado.client

import click.dobel.tado.model.HomeInfo
import click.dobel.tado.model.User
import click.dobel.tado.model.WeatherReport
import click.dobel.tado.model.Zone
import click.dobel.tado.model.ZoneState
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import io.reactivex.Maybe

@Client(TadoApiClient.SERVER, path = TadoApiClient.BASE_URL)
interface TadoApiClient {

  companion object {
    const val SERVER = "https://my.tado.com"
    const val BASE_URL = "/api/v2"
  }

  @Get("/me")
  fun me(): Maybe<User>

  @Get("/homes/{homeId}")
  fun homes(homeId: Int): Maybe<HomeInfo>

  @Get("/homes/{homeId}/zones")
  fun zones(homeId: Int): Maybe<List<Zone>>

  /* calls to these methods are triggered by micrometer when refreshing all metered values.
   * since one call contains the data for multiple gauges,
   * cache it for "micrometer refresh time - a few secs" */
  @Cacheable(cacheNames = ["tado-zonestate"], atomic = true)
  @Get("/homes/{homeId}/zones/{zoneId}/state")
  fun zoneState(homeId: Int, zoneId: Int): Maybe<ZoneState>

  @Cacheable(cacheNames = ["tado-weather"], atomic = true)
  @Get("/homes/{homeId}/weather")
  fun weather(homeId: Int): Maybe<WeatherReport>
}
