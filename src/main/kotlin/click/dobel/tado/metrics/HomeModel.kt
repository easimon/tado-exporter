package click.dobel.tado.metrics

import click.dobel.tado.model.HomeInfo
import click.dobel.tado.model.Zone
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Memory model to store references to all homes and corresponding zones.
 */
data class HomeModel(
  val homes: Map<Int, HomeInfo>
) {
  private val homeZones: ConcurrentMap<Int, MutableSet<Zone>> = ConcurrentHashMap()

  fun homeZones(homeId: Int): MutableSet<Zone> = homeZones.getOrPut(homeId) {
    mutableSetOf()
  }
}
