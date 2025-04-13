package click.dobel.tado.exporter.metrics

import click.dobel.tado.exporter.apiclient.TadoApiClient
import mu.KLogging
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicReference

@Component
class HomeModelRefresher(
  private val tadoMeterFactory: TadoMeterFactory,
  private val tadoApiClient: TadoApiClient
) {
  companion object : KLogging()

  private fun initializedHomeModel(): HomeModel {
    return homeModelRef.updateAndGet { current ->
      if (!current.isEmpty) {
        current
      } else {
        if (!tadoApiClient.isAuthenticated) {
          logger.info { "Not authenticated, skipping home model initialization." }
          current
        } else {
          logger.info { "Initializing homes from API." }
          HomeModel(
            tadoApiClient.me().homes
              .map { userHomes -> tadoMeterFactory.createHomeMeters(userHomes) }
              .associateBy { userHomes -> userHomes.id }
          ).also { result ->
            logger.info { "${result.homes.size} homes initialized." }
          }
        }
      }
    }
  }

  private var homeModelRef = AtomicReference(HomeModel(emptyMap()))

  val isInitialized: Boolean get() = !homeModelRef.get().isEmpty

  fun refreshHomeModel() {
    val homeModel = initializedHomeModel()
    if (!homeModel.isEmpty) {
      logger.info("Refreshing zones for all known homes.")

      homeModel.homes.values.forEach { userHomes ->
        logger.info { "Refreshing zones for home '${userHomes.name}' (${userHomes.id})." }

        val allZones = try {
          tadoApiClient.zones(userHomes.id)
        } catch (ex: Exception) {
          logger.warn(ex) { "Failed to refresh zones for user '${userHomes.name}', clearing zone list." }
          emptyList()
        }
        val newZoneEntries = homeModel.updateHomeZones(userHomes, allZones)

        tadoMeterFactory.createZoneMeters(userHomes, newZoneEntries)
      }
    }
  }
}
