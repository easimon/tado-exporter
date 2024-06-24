package click.dobel.tado.exporter.metrics

import click.dobel.tado.exporter.apiclient.TadoApiClient
import mu.KLogging
import org.springframework.stereotype.Component

@Component
class HomeModelRefresher(
  private val tadoMeterFactory: TadoMeterFactory,
  private val tadoApiClient: TadoApiClient
) {
  companion object : KLogging() {

    private fun initializeHomeModel(
      tadoMeterFactory: TadoMeterFactory,
      tadoApiClient: TadoApiClient
    ): HomeModel {
      logger.info("Initializing homes from API.")
      val result = HomeModel(
        tadoApiClient.me().homes
          .map { userHomes -> tadoMeterFactory.createHomeMeters(userHomes) }
          .map { userHomes -> userHomes.id to userHomes }
          .toMap()
      )

      logger.info { "${result.homes.size} homes initialized." }
      return result
    }
  }

  private val homeModel: HomeModel by lazy(this) {
    initializeHomeModel(tadoMeterFactory, tadoApiClient)
  }

  fun refreshHomeModel() {
    logger.info("Refreshing zones for all known homes.")

    homeModel.homes.values.forEach { userHomes ->
      logger.info {
        "Refreshing zones for home '${userHomes.name}' (${userHomes.id})."
      }

      val allZones = tadoApiClient.zones(userHomes.id)
      val newZoneEntries = homeModel.updateHomeZones(userHomes, allZones)

      tadoMeterFactory.createZoneMeters(userHomes, newZoneEntries)
    }
  }
}
