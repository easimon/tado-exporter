package click.dobel.tado.metrics

import click.dobel.tado.client.TadoApiClient
import click.dobel.tado.util.logger
import javax.inject.Singleton

@Singleton
class HomeModelRefresher(
  private val tadoMeterFactory: TadoMeterFactory,
  private val tadoApiClient: TadoApiClient
) {

  companion object {
    private val LOGGER = logger()

    private fun initializeHomeModel(
      tadoMeterFactory: TadoMeterFactory,
      tadoApiClient: TadoApiClient
    ): HomeModel {
      LOGGER.info("Initializing homes from API.")
      val result = HomeModel(
        tadoApiClient.me().homes
          .map { userHome -> tadoApiClient.homes(userHome.id) }
          .map { homeInfo -> tadoMeterFactory.createHomeMeters(homeInfo) }
          .map { homeInfo -> homeInfo.id to homeInfo }
          .toMap()
      )

      LOGGER.info("{} homes initialized.", result.homes.size)
      return result
    }
  }

  private val homeModel: HomeModel by lazy(this) {
    initializeHomeModel(tadoMeterFactory, tadoApiClient)
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
