package click.dobel.tado.exporter.metrics

import click.dobel.tado.api.UserHomes
import click.dobel.tado.api.Zone
import click.dobel.tado.exporter.apiclient.model.toEntrySet
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

  private val homeZones: ConcurrentMap<Int, MutableSet<ZoneEntry>> = ConcurrentHashMap()

  private fun homeZones(homeId: Int): MutableSet<ZoneEntry> = homeZones.getOrPut(homeId) {
    mutableSetOf()
  }

  fun updateHomeZones(userHomes: UserHomes, newZones: Collection<Zone>): Set<ZoneEntry> {
    /* since the model is the source of "references to things being monitored by micrometer",
    * try to modify it in a minimal way */
    val knownZoneEntries = homeZones(userHomes.id)
    val newZoneEntries = newZones.toEntrySet()
    val zoneEntriesToAdd = newZoneEntries.filter { !knownZoneEntries.contains(it) }.toSet()
    val zoneEntriesToDelete = knownZoneEntries.filter { !newZoneEntries.contains(it) }.toSet()

    logZones("Old zones", knownZoneEntries)
    logZones("New zones", newZoneEntries)
    logZones("To Add   ", zoneEntriesToAdd)
    logZones("To Delete", zoneEntriesToDelete)

    knownZoneEntries.removeAll(zoneEntriesToDelete)
    knownZoneEntries.addAll(zoneEntriesToAdd)

    LOGGER.info(
      "Zones for home '{}' ({}) updated, {} zones total, {} zones added, {} zones deleted.",
      userHomes.name,
      userHomes.id,
      knownZoneEntries.size,
      zoneEntriesToAdd.size,
      zoneEntriesToDelete.size
    )

    return zoneEntriesToAdd
  }

  private fun logZones(prefix: String, zones: Collection<ZoneEntry>) {
    LOGGER.debug(
      "${prefix}: {} ({})",
      zones.size,
      zones.joinToString(separator = ", ") { "${it.id}: ${it.name}" }
    )
  }
}
