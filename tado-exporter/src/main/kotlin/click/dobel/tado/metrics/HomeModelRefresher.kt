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
          .map { userHomes -> tadoMeterFactory.createHomeMeters(userHomes) }
          .map { userHomes -> userHomes.id to userHomes }
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

    homeModel.homes.values.forEach { userHomes ->
      LOGGER.info(
        "Refreshing zones for home '{}' ({}).",
        userHomes.name,
        userHomes.id
      )

      val allZones = tadoApiClient.zones(userHomes.id)
      val newZones = homeModel.updateHomeZones(userHomes, allZones)

      tadoMeterFactory.createZoneMeters(userHomes, newZones)
    }
  }
}
