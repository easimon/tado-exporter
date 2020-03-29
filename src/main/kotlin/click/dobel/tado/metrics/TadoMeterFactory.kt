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
import io.micrometer.core.instrument.Gauge
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

    private enum class BaseUnit(
      val stringValue: String
    ) {
      PERCENT("percent"),
      CELSIUS("celsius"),
      FAHRENHEIT("fahrenheit"),
      BOOLEAN("boolean")
    }
  }

  fun createHomeMeters(home: HomeInfo): HomeInfo {
    LOGGER.info("Adding gauges for weather information for home '{}' ({})", home.name, home.id)

    val homeTags = homeTags(home)

    registerGauge(
      "solar_intensity_percentage",
      "Current solar intensity at the home's location.",
      BaseUnit.PERCENT,
      homeTags,
      home) { h ->
      tadoApiClient.weather(h.id).solarIntensity.percentage
    }
    registerGauge(
      "temperature_outside_celsius",
      "Current outside temperature at the home's location, in degrees celsius.",
      BaseUnit.CELSIUS,
      homeTags,
      home) { h ->
      tadoApiClient.weather(h.id).outsideTemperature.celsius
    }
    registerGauge(
      "temperature_outside_fahrenheit",
      "Current outside temperature at the home's location, in degrees fahrenheit.",
      BaseUnit.FAHRENHEIT,
      homeTags,
      home) { h ->
      tadoApiClient.weather(h.id).outsideTemperature.fahrenheit
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
    registerGauge(
      "temperature_measured_celsius",
      "The currently measured temperature in degrees celsius.",
      BaseUnit.CELSIUS,
      zoneTags,
      zone) { z ->
      tadoApiClient.zoneState(home.id, z.id).sensorDataPoints.insideTemperature.celsius
    }
    registerGauge("" +
      "temperature_measured_fahrenheit",
      "The currently measured temperature in degrees fahrenheit.",
      BaseUnit.FAHRENHEIT,
      zoneTags,
      zone) { z ->
      tadoApiClient.zoneState(home.id, z.id).sensorDataPoints.insideTemperature.fahrenheit
    }
    registerGauge(
      "humidity_measured_percentage",
      "The currently measured humidity in percent.",
      BaseUnit.PERCENT,
      zoneTags,
      zone) { z ->
      tadoApiClient.zoneState(home.id, z.id).sensorDataPoints.humidity.percentage
    }
    registerBooleanGauge(
      "is_window_open",
      "Window open detection, 1 if window is open, 0 otherwise.",
      BaseUnit.BOOLEAN,
      zoneTags,
      zone) { z ->
      tadoApiClient.zoneState(home.id, z.id).isOpenWindowDetected == true
    }
  }

  private fun createHeatingZoneMeters(home: HomeInfo, zone: Zone, zoneTags: Tags) {
    LOGGER.info("Adding gauges for heating zone '{}' ({}).", zone.name, zone.id)
    createGenericZoneMeters(home, zone, zoneTags)
    registerGauge(
      "heating_power_percentage",
      "Heating power percentage.",
      BaseUnit.PERCENT,
      zoneTags,
      zone) { z ->
      tadoApiClient.zoneState(home.id, z.id).activityDataPoints.heatingPower.percentage
    }
    registerGauge(
      "temperature_set_celsius",
      "The current target temperature in degrees celsius.",
      BaseUnit.CELSIUS,
      zoneTags,
      zone) { z ->
      (tadoApiClient.zoneState(home.id, z.id).setting as HeatingZoneSetting).temperature.celsius
    }
    registerGauge(
      "temperature_set_fahrenheit",
      "The current target temperature in degrees fahrenheit.",
      BaseUnit.FAHRENHEIT,
      zoneTags,
      zone) { z ->
      (tadoApiClient.zoneState(home.id, z.id).setting as HeatingZoneSetting).temperature.fahrenheit
    }
    registerBooleanGauge(
      "is_zone_powered",
      "Zone power state. 1 if powered, 0 otherwise",
      BaseUnit.BOOLEAN,
      zoneTags,
      zone) { z ->
      (tadoApiClient.zoneState(home.id, z.id).setting as HeatingZoneSetting).power == Power.ON
    }
  }

  private fun createCoolingZoneMeters(home: HomeInfo, zone: Zone, zoneTags: Tags) {
    // TODO: check if these values are available in AC zones.
    LOGGER.info("Adding gauges for AC zone '{}' ({}).", zone.name, zone.id)
    createGenericZoneMeters(home, zone, zoneTags)
    registerGauge(
      "temperature_set_celsius",
      "The current target temperature in degrees celsius.",
      BaseUnit.CELSIUS,
      zoneTags,
      zone) { z ->
      (tadoApiClient.zoneState(home.id, z.id).setting as CoolingZoneSetting).temperature.celsius
    }
    registerGauge(
      "temperature_set_fahrenheit",
      "The current target temperature in degrees fahrenheit.",
      BaseUnit.FAHRENHEIT,
      zoneTags,
      zone) { z ->
      (tadoApiClient.zoneState(home.id, z.id).setting as CoolingZoneSetting).temperature.fahrenheit
    }
    registerBooleanGauge(
      "is_zone_powered",
      "Zone power state. 1 if powered, 0 otherwise",
      BaseUnit.BOOLEAN,
      zoneTags,
      zone) { z ->
      (tadoApiClient.zoneState(home.id, z.id).setting as CoolingZoneSetting).power == Power.ON
    }
  }

  private fun createHotWaterZoneMeters(home: HomeInfo, zone: Zone, zoneTags: Tags) {
    LOGGER.info("Adding gauges for hot water zone '{}' ({}).", zone.name, zone.id)
    registerBooleanGauge(
      "is_zone_powered",
      "Zone power state. 1 if powered, 0 otherwise",
      BaseUnit.BOOLEAN,
      zoneTags,
      zone) { z ->
      (tadoApiClient.zoneState(home.id, z.id).setting as HotWaterZoneSetting).power == Power.ON
    }
  }

  private fun gaugeName(name: String) = PREFIX + name

  private fun <T : Any> registerGauge(
    name: String,
    description: String,
    baseUnit: BaseUnit,
    tags: Tags,
    item: T,
    getter: (T) -> Number
  ) = Gauge
    .builder(gaugeName(name), item, numberToDouble(getter))
    .tags(tags)
    .description(description)
    .baseUnit(baseUnit.stringValue)
    .register(meterRegistry)

  private fun <T : Any> registerBooleanGauge(
    name: String,
    description: String,
    baseUnit: BaseUnit,
    tags: Tags,
    item: T,
    getter: (T) -> Boolean
  ) = registerGauge(name, description, baseUnit, tags, item, booleanToDouble(getter))

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
