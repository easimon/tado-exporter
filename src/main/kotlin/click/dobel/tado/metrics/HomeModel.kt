package click.dobel.tado.metrics

import click.dobel.tado.logger
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

  companion object {
    private val LOGGER = logger()
  }

  private val homeZones: ConcurrentMap<Int, MutableSet<Zone>> = ConcurrentHashMap()

  private fun homeZones(homeId: Int): MutableSet<Zone> = homeZones.getOrPut(homeId) {
    mutableSetOf()
  }

  fun updateHomeZones(homeInfo: HomeInfo, newZones: Collection<Zone>): Set<Zone> {
    /* since the model is the source of "references to things being monitored by micrometer",
    * try to modify it in a minimal way */
    val knownZones = homeZones(homeInfo.id)
    val zonesToAdd = newZones.filter { !knownZones.contains(it) }.toSet()
    val zonesToDelete = knownZones.filter { !newZones.contains(it) }.toSet()

    knownZones.removeAll(zonesToDelete)
    knownZones.addAll(zonesToAdd)

    LOGGER.info(
      "Zones for home '{}' ({}) updated, {} zones added, {} zones deleted.",
      homeInfo.name,
      homeInfo.id,
      zonesToAdd.size,
      zonesToDelete.size
    )

    return zonesToAdd
  }
}
