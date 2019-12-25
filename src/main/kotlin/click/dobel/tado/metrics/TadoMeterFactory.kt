package click.dobel.tado.metrics

import click.dobel.tado.client.TadoApiClient
import click.dobel.tado.logger
import click.dobel.tado.model.CoolingZoneSetting
import click.dobel.tado.model.HeatingZoneSetting
import click.dobel.tado.model.HomeInfo
import click.dobel.tado.model.HotWaterZoneSetting
import click.dobel.tado.model.Power
import click.dobel.tado.model.TadoSystemType
import click.dobel.tado.model.Zone
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Tags
import javax.inject.Singleton

@Singleton
class TadoMeterFactory(
  private val meterRegistry: MeterRegistry,
  private val tadoApiClient: TadoApiClient
) {
  companion object {
    private const val PREFIX = "tado_"
    private val LOGGER = logger()
  }

  fun createHomeMeters(home: HomeInfo): HomeInfo {
    LOGGER.info("Adding gauges for weather information for home '{}' ({})", home.name, home.id)

    val homeTags = homeTags(home)

    gauge("solar_intensity_percentage", homeTags, home) { h ->
      tadoApiClient.weather(h.id).blockingGet().solarIntensity.percentage
    }
    gauge("temperature_outside_celsius", homeTags, home) { h ->
      tadoApiClient.weather(h.id).blockingGet().outsideTemperature.celsius
    }
    gauge("temperature_outside_fahrenheit", homeTags, home) { h ->
      tadoApiClient.weather(h.id).blockingGet().outsideTemperature.fahrenheit
    }
    return home
  }

  fun createZoneMeters(home: HomeInfo, zones: List<Zone>) {
    zones.forEach { zone ->
      val zoneTags = zoneTags(home, zone)

      when (zone.type) {
        TadoSystemType.HEATING -> {
          createHeatingZoneMeters(home, zone, zoneTags)
        }
        TadoSystemType.AIR_CONDITIONING -> {
          createCoolingZoneMeters(home, zone, zoneTags)
        }
        TadoSystemType.HOT_WATER -> {
          createHotWaterZoneMeters(home, zone, zoneTags)
        }
        else -> {
          LOGGER.warn("Unknown zone type {} for zone '{}' ({}).", zone.type, zone.name, zone.id)
        }
      }
    }
  }

  private fun createGenericZoneMeters(home: HomeInfo, zone: Zone, zoneTags: Tags) {
    gauge("temperature_measured_celsius", zoneTags, zone) { z ->
      tadoApiClient.zoneState(home.id, z.id).blockingGet().sensorDataPoints.insideTemperature.celsius
    }
    gauge("temperature_measured_fahrenheit", zoneTags, zone) { z ->
      tadoApiClient.zoneState(home.id, z.id).blockingGet().sensorDataPoints.insideTemperature.fahrenheit
    }
    gauge("humidity_measured_percentage", zoneTags, zone) { z ->
      tadoApiClient.zoneState(home.id, z.id).blockingGet().sensorDataPoints.humidity.percentage
    }
    boolGauge("is_window_open", zoneTags, zone) { z ->
      tadoApiClient.zoneState(home.id, z.id).blockingGet().isOpenWindowDetected == true
    }
  }

  private fun createHeatingZoneMeters(home: HomeInfo, zone: Zone, zoneTags: Tags) {
    LOGGER.info("Adding gauges for heating zone '{}' ({}).", zone.name, zone.id)
    createGenericZoneMeters(home, zone, zoneTags)
    gauge("heating_power_percentage", zoneTags, zone) { z ->
      tadoApiClient.zoneState(home.id, z.id).blockingGet().activityDataPoints.heatingPower.percentage
    }
    gauge("temperature_set_celsius", zoneTags, zone) { z ->
      (tadoApiClient.zoneState(home.id, z.id).blockingGet().setting as HeatingZoneSetting).temperature.celsius
    }
    gauge("temperature_set_fahrenheit", zoneTags, zone) { z ->
      (tadoApiClient.zoneState(home.id, z.id).blockingGet().setting as HeatingZoneSetting).temperature.fahrenheit
    }
    boolGauge("is_zone_powered", zoneTags, zone) { z ->
      (tadoApiClient.zoneState(home.id, z.id).blockingGet().setting as HeatingZoneSetting).power == Power.ON
    }
  }

  private fun createCoolingZoneMeters(home: HomeInfo, zone: Zone, zoneTags: Tags) {
    // TODO: check if these values are available in AC zones.
    LOGGER.info("Adding gauges for AC zone '{}' ({}).", zone.name, zone.id)
    createGenericZoneMeters(home, zone, zoneTags)
    gauge("temperature_set_celsius", zoneTags, zone) { z ->
      (tadoApiClient.zoneState(home.id, z.id).blockingGet().setting as CoolingZoneSetting).temperature.celsius
    }
    gauge("temperature_set_fahrenheit", zoneTags, zone) { z ->
      (tadoApiClient.zoneState(home.id, z.id).blockingGet().setting as CoolingZoneSetting).temperature.fahrenheit
    }
    boolGauge("is_zone_powered", zoneTags, zone) { z ->
      (tadoApiClient.zoneState(home.id, z.id).blockingGet().setting as CoolingZoneSetting).power == Power.ON
    }
  }

  private fun createHotWaterZoneMeters(home: HomeInfo, zone: Zone, zoneTags: Tags) {
    LOGGER.info("Adding gauges for hot water zone '{}' ({}).", zone.name, zone.id)
    boolGauge("is_zone_powered", zoneTags, zone) { z ->
      (tadoApiClient.zoneState(home.id, z.id).blockingGet().setting as HotWaterZoneSetting).power == Power.ON
    }
  }

  private fun gaugeName(name: String) = PREFIX + name

  private fun <T : Any> gauge(name: String, tags: Tags, item: T, getter: (T) -> Number) =
    meterRegistry.gauge(gaugeName(name), tags, item, numberToDouble(getter))

  private fun <T : Any> boolGauge(name: String, tags: Tags, item: T, getter: (T) -> Boolean) =
    meterRegistry.gauge(gaugeName(name), tags, item, booleanToDouble(getter))

  private fun <T : Any> numberToDouble(f: (T) -> Number): (T) -> Double = { n ->
    f(n).toDouble()
  }

  private fun <T : Any> booleanToDouble(f: (T) -> Boolean): (T) -> Double = { b ->
    if (f(b)) 1.0 else 0.0
  }

  private fun homeTags(home: HomeInfo): Tags {
    return Tags.of(
      Tag.of("home_id", home.id.toString())
    )
  }

  private fun zoneTags(home: HomeInfo, zone: Zone): Tags {
    return homeTags(home)
      .and(
        Tags.of(
          Tag.of("zone_id", zone.id.toString()),
          Tag.of("zone_name", zone.name),
          Tag.of("zone_type", zone.type.toString())
        )
      )
  }
}
