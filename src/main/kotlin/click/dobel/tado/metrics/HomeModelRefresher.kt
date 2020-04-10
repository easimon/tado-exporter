package click.dobel.tado.metrics

import click.dobel.tado.client.TadoApiClient
import click.dobel.tado.logger
import javax.inject.Singleton

@Singleton
class HomeModelRefresher(
  private val tadoMeterFactory: TadoMeterFactory,
  private val tadoApiClient: TadoApiClient
) {

  companion object {
    private val LOGGER = logger()
  }

  lateinit var homeModel: HomeModel

  fun initializeHomeModel() {
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

  fun refreshHomeModel() {
    LOGGER.info("Refreshing zones for all known homes.")

    homeModel.homes.values.forEach { homeInfo ->
      LOGGER.info(
        "Refreshing zones for home '{}' ({}).",
        homeInfo.name,
        homeInfo.id
      )

      val allZones = tadoApiClient.zones(homeInfo.id)
      val newZones = homeModel.updateHomeZones(homeInfo, allZones)

      tadoMeterFactory.createZoneMeters(homeInfo, newZones)
    }
  }
}
