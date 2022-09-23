package click.dobel.tado.exporter.metrics

import click.dobel.tado.api.CoolingZoneSetting
import click.dobel.tado.api.HeatingZoneSetting
import click.dobel.tado.api.HotWaterZoneSetting
import click.dobel.tado.api.Power
import click.dobel.tado.api.TadoSystemType
import click.dobel.tado.api.UserHomes
import click.dobel.tado.api.Zone
import click.dobel.tado.exporter.apiclient.TadoApiClient
import click.dobel.tado.exporter.metrics.TadoMeterFactory.Companion.TAG_HOME_ID
import click.dobel.tado.exporter.metrics.TadoMeterFactory.Companion.TAG_ZONE_ID
import click.dobel.tado.exporter.metrics.TadoMeterFactory.Companion.TAG_ZONE_NAME
import click.dobel.tado.exporter.metrics.TadoMeterFactory.Companion.TAG_ZONE_TYPE
import click.dobel.tado.util.logger
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Tags
import org.springframework.stereotype.Component

@Component
class TadoMeterFactory(
  private val meterRegistry: MeterRegistry,
  private val tadoApiClient: TadoApiClient
) {
  companion object {
    private val LOGGER = logger()
    internal const val PREFIX = "tado_"

    internal const val SOLAR_INTENSITY_PERCENTAGE = "solar_intensity_percentage"
    internal const val TEMPERATURE_OUTSIDE_CELSIUS = "temperature_outside_celsius"
    internal const val TEMPERATURE_OUTSIDE_FAHRENHEIT = "temperature_outside_fahrenheit"

    internal const val TEMPERATURE_MEASURED_CELSIUS = "temperature_measured_celsius"
    internal const val TEMPERATURE_MEASURED_FAHRENHEIT = "temperature_measured_fahrenheit"
    internal const val HUMIDITY_MEASURED_PERCENTAGE = "humidity_measured_percentage"
    internal const val IS_WINDOW_OPEN = "is_window_open"

    internal const val HEATING_POWER_PERCENTAGE = "heating_power_percentage"
    internal const val TEMPERATURE_SET_CELSIUS = "temperature_set_celsius"
    internal const val TEMPERATURE_SET_FAHRENHEIT = "temperature_set_fahrenheit"
    internal const val IS_ZONE_POWERED = "is_zone_powered"

    internal const val TAG_HOME_ID = "home_id"
    internal const val TAG_ZONE_ID = "zone_id"
    internal const val TAG_ZONE_NAME = "zone_name"
    internal const val TAG_ZONE_TYPE = "zone_type"
  }

  fun createHomeMeters(home: UserHomes): UserHomes {
    LOGGER.info("Adding gauges for weather information for home '{}' ({})", home.name, home.id)

    val homeTags = homeTags(home)

    registerGauge(
      SOLAR_INTENSITY_PERCENTAGE,
      "Current solar intensity at the home's location.",
      homeTags,
      home
    ) { h ->
      tadoApiClient.weather(h.id).solarIntensity.percentage
    }
    registerGauge(
      TEMPERATURE_OUTSIDE_CELSIUS,
      "Current outside temperature at the home's location, in degrees celsius.",
      homeTags,
      home
    ) { h ->
      tadoApiClient.weather(h.id).outsideTemperature.celsius
    }
    registerGauge(
      TEMPERATURE_OUTSIDE_FAHRENHEIT,
      "Current outside temperature at the home's location, in degrees fahrenheit.",
      homeTags,
      home
    ) { h ->
      tadoApiClient.weather(h.id).outsideTemperature.fahrenheit
    }
    return home
  }

  fun createZoneMeters(home: UserHomes, zones: Collection<Zone>) {
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

  private fun createGenericZoneMeters(home: UserHomes, zone: Zone, zoneTags: Tags) {
    registerGauge(
      TEMPERATURE_MEASURED_CELSIUS,
      "The currently measured temperature in degrees celsius.",
      zoneTags,
      zone
    ) { z ->
      tadoApiClient.zoneState(home.id, z.id).sensorDataPoints.insideTemperature.celsius
    }
    registerGauge(
      "" +
        TEMPERATURE_MEASURED_FAHRENHEIT,
      "The currently measured temperature in degrees fahrenheit.",
      zoneTags,
      zone
    ) { z ->
      tadoApiClient.zoneState(home.id, z.id).sensorDataPoints.insideTemperature.fahrenheit
    }
    registerGauge(
      HUMIDITY_MEASURED_PERCENTAGE,
      "The currently measured humidity in percent.",
      zoneTags,
      zone
    ) { z ->
      tadoApiClient.zoneState(home.id, z.id).sensorDataPoints.humidity.percentage
    }
    registerBooleanGauge(
      IS_WINDOW_OPEN,
      "Window open detection, 1 if window is open, 0 otherwise.",
      zoneTags,
      zone
    ) { z ->
      val state = tadoApiClient.zoneState(home.id, z.id)
      state.isOpenWindowDetected == true || state.openWindow != null
    }
  }

  private fun createHeatingZoneMeters(home: UserHomes, zone: Zone, zoneTags: Tags) {
    LOGGER.info("Adding gauges for heating zone '{}' ({}).", zone.name, zone.id)
    createGenericZoneMeters(home, zone, zoneTags)
    registerGauge(
      HEATING_POWER_PERCENTAGE,
      "Heating power percentage.",
      zoneTags,
      zone
    ) { z ->
      tadoApiClient.zoneState(home.id, z.id).activityDataPoints.heatingPower.percentage
    }
    registerGauge(
      TEMPERATURE_SET_CELSIUS,
      "The current target temperature in degrees celsius.",
      zoneTags,
      zone
    ) { z ->
      (tadoApiClient.zoneState(home.id, z.id).setting as HeatingZoneSetting).temperature?.celsius
    }
    registerGauge(
      TEMPERATURE_SET_FAHRENHEIT,
      "The current target temperature in degrees fahrenheit.",
      zoneTags,
      zone
    ) { z ->
      (tadoApiClient.zoneState(home.id, z.id).setting as HeatingZoneSetting).temperature?.fahrenheit
    }
    registerBooleanGauge(
      IS_ZONE_POWERED,
      "Zone power state. 1 if powered, 0 otherwise",
      zoneTags,
      zone
    ) { z ->
      (tadoApiClient.zoneState(home.id, z.id).setting as HeatingZoneSetting).power == Power.ON
    }
  }

  private fun createCoolingZoneMeters(home: UserHomes, zone: Zone, zoneTags: Tags) {
    // TODO: check if these values are available in AC zones.
    LOGGER.info("Adding gauges for AC zone '{}' ({}).", zone.name, zone.id)
    createGenericZoneMeters(home, zone, zoneTags)
    registerGauge(
      TEMPERATURE_SET_CELSIUS,
      "The current target temperature in degrees celsius.",
      zoneTags,
      zone
    ) { z ->
      (tadoApiClient.zoneState(home.id, z.id).setting as CoolingZoneSetting).temperature?.celsius
    }
    registerGauge(
      TEMPERATURE_SET_FAHRENHEIT,
      "The current target temperature in degrees fahrenheit.",
      zoneTags,
      zone
    ) { z ->
      (tadoApiClient.zoneState(home.id, z.id).setting as CoolingZoneSetting).temperature?.fahrenheit
    }
    registerBooleanGauge(
      IS_ZONE_POWERED,
      "Zone power state. 1 if powered, 0 otherwise",
      zoneTags,
      zone
    ) { z ->
      (tadoApiClient.zoneState(home.id, z.id).setting as CoolingZoneSetting).power == Power.ON
    }
  }

  private fun createHotWaterZoneMeters(home: UserHomes, zone: Zone, zoneTags: Tags) {
    LOGGER.info("Adding gauges for hot water zone '{}' ({}).", zone.name, zone.id)
    registerBooleanGauge(
      IS_ZONE_POWERED,
      "Zone power state. 1 if powered, 0 otherwise",
      zoneTags,
      zone
    ) { z ->
      (tadoApiClient.zoneState(home.id, z.id).setting as HotWaterZoneSetting).power == Power.ON
    }
  }

  private fun gaugeName(name: String) = PREFIX + name

  private fun <T : Any> registerGauge(
    name: String,
    description: String,
    tags: Tags,
    item: T,
    getter: (T) -> Number?
  ) = Gauge
    .builder(gaugeName(name), item, numberToDouble(getter))
    .tags(tags)
    .description(description)
    .register(meterRegistry)

  private fun <T : Any> registerBooleanGauge(
    name: String,
    description: String,
    tags: Tags,
    item: T,
    getter: (T) -> Boolean?
  ) = registerGauge(name, description, tags, item, booleanToDouble(getter))

  private fun <T : Any> numberToDouble(f: (T) -> Number?): (T) -> Double = { n ->
    f(n)?.toDouble() ?: Double.NaN
  }

  private fun <T : Any> booleanToDouble(f: (T) -> Boolean?): (T) -> Double = numberToDouble { b ->
    when (f(b)) {
      true -> 1.0
      false -> 0.0
      null -> null
    }
  }
}

internal fun homeTags(home: UserHomes) =
  Tags.of(
    Tag.of(TAG_HOME_ID, home.id.toString())
  )

internal fun zoneTags(home: UserHomes, zone: Zone) =
  homeTags(home).and(
    Tags.of(
      Tag.of(TAG_ZONE_ID, zone.id.toString()),
      Tag.of(TAG_ZONE_NAME, zone.name),
      Tag.of(TAG_ZONE_TYPE, zone.type.toString())
    )
  )