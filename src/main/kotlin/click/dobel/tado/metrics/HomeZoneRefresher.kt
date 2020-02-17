package click.dobel.tado.metrics

import click.dobel.tado.client.TadoApiClient
import click.dobel.tado.logger
import io.micronaut.scheduling.annotation.Scheduled
import javax.inject.Singleton

@Singleton
class HomeZoneRefresher(
  private val tadoMeterFactory: TadoMeterFactory,
  private val tadoApiClient: TadoApiClient
) {

  companion object {
    private val LOGGER = logger()
  }

  private val homeModel: HomeModel

  init {
    LOGGER.info("Initializing homes from API.")
    homeModel = HomeModel(
      tadoApiClient.me().homes
        .map { userHome -> tadoApiClient.homes(userHome.id) }
        .map { homeInfo -> tadoMeterFactory.createHomeMeters(homeInfo) }
        .map { homeInfo -> homeInfo.id to homeInfo }
        .toMap()
    )
    LOGGER.info("{} homes initialized.", homeModel.homes.size)
  }

  @Scheduled(fixedRate = "\${tado.zone-discovery-interval}")
  fun refreshHomes() {
    LOGGER.info("Refreshing zones for all homes.")

    homeModel.homes.values.forEach { homeInfo ->
      LOGGER.info(
        "Refreshing zones for home '{}' ({}).",
        homeInfo.name,
        homeInfo.id
      )

      /* since the model is the source of "references to things being monitored by micrometer",
      * try to modify it in a minimal way */
      val knownZones = homeModel.homeZones(homeInfo.id)
      val allZones = tadoApiClient.zones(homeInfo.id)
      val zonesToAdd = allZones.filter { !knownZones.contains(it) }
      val zonesToDelete = knownZones.filter { !allZones.contains(it) }

      tadoMeterFactory.createZoneMeters(homeInfo, zonesToAdd)
      knownZones.addAll(zonesToAdd)
      knownZones.removeAll(zonesToDelete)
      LOGGER.info(
        "Zones for home '{}' ({}) updated, {} zones added, {} zones deleted.",
        homeInfo.name,
        homeInfo.id,
        zonesToAdd.size,
        zonesToDelete.size
      )
    }
  }
}
