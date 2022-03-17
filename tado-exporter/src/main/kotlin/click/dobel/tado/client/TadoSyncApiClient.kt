package click.dobel.tado.client

import click.dobel.tado.util.logger
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import jakarta.inject.Singleton
import reactor.kotlin.core.publisher.toMono

@Singleton
open class TadoSyncApiClient(
  private val tadoApiClient: TadoApiClient,
) {

  companion object {
    private const val CACHE_NAME_ZONESTATE = "tado-zonestate"
    private const val CACHE_NAME_WEATHER = "tado-weather"
  }

  fun me() = tadoApiClient.me().block()!!
  fun homes(homeId: Int) = tadoApiClient.homes(homeId).block()!!
  fun zones(homeId: Int) = tadoApiClient.zones(homeId).block()!!

  /* calls to these methods are triggered by micrometer when refreshing all metered values.
   * since one call contains the data for multiple gauges,
   * cache it for "micrometer refresh interval minus a few secs" */
  @Cacheable(cacheNames = [CACHE_NAME_ZONESTATE], atomic = true)
  open fun zoneState(homeId: Int, zoneId: Int) = tadoApiClient.zoneState(homeId, zoneId).block()!!

  @Cacheable(cacheNames = [CACHE_NAME_WEATHER], atomic = true)
  open fun weather(homeId: Int) = tadoApiClient.weather(homeId).block()!!
}
