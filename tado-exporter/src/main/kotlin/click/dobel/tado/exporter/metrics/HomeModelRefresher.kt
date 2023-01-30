package click.dobel.tado.exporter.metrics

import click.dobel.tado.exporter.apiclient.TadoApiClient
import click.dobel.tado.util.logger
import org.springframework.stereotype.Component

@Component
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
      val newZoneEntries = homeModel.updateHomeZones(userHomes, allZones)

      tadoMeterFactory.createZoneMeters(userHomes, newZoneEntries)
    }
  }
}
