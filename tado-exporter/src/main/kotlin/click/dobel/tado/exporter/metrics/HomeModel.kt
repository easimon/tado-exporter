package click.dobel.tado.exporter.metrics

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

    logZones("Old zones", knownZones)
    logZones("New zones", newZones)
    logZones("To Add   ", zonesToAdd)
    logZones("To Delete", zonesToDelete)

    knownZones.removeAll(zonesToDelete)
    knownZones.addAll(zonesToAdd)

    LOGGER.info(
      "Zones for home '{}' ({}) updated, {} zones total, {} zones added, {} zones deleted.",
      userHomes.name,
      userHomes.id,
      knownZones.size,
      zonesToAdd.size,
      zonesToDelete.size
    )

    return zonesToAdd
  }

  private fun logZones(prefix: String, zones: Collection<Zone>) {
    LOGGER.debug(
      "${prefix}: {} ({})",
      zones.size,
      zones.joinToString(separator = ", ") { "${it.id}: ${it.name}" }
    )
  }
}
