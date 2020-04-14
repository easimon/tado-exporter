package click.dobel.tado.metrics

import click.dobel.tado.api.UserHomes
import click.dobel.tado.api.Zone
import click.dobel.tado.util.logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Memory model to store references to all homes and corresponding zones.
 */
data class HomeModel(
  val homes: Map<Int, UserHomes>
) {

  companion object {
    private val LOGGER = logger()
  }

  private val homeZones: ConcurrentMap<Int, MutableSet<Zone>> = ConcurrentHashMap()

  private fun homeZones(homeId: Int): MutableSet<Zone> = homeZones.getOrPut(homeId) {
    mutableSetOf()
  }

  fun updateHomeZones(userHomes: UserHomes, newZones: Collection<Zone>): Set<Zone> {
    /* since the model is the source of "references to things being monitored by micrometer",
    * try to modify it in a minimal way */
    val knownZones = homeZones(userHomes.id)
    val zonesToAdd = newZones.filter { !knownZones.contains(it) }.toSet()
    val zonesToDelete = knownZones.filter { !newZones.contains(it) }.toSet()

    knownZones.removeAll(zonesToDelete)
    knownZones.addAll(zonesToAdd)

    LOGGER.info(
      "Zones for home '{}' ({}) updated, {} zones added, {} zones deleted.",
      userHomes.name,
      userHomes.id,
      zonesToAdd.size,
      zonesToDelete.size
    )

    return zonesToAdd
  }
}
