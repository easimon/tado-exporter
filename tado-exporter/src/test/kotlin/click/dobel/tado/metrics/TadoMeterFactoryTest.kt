package click.dobel.tado.metrics

import click.dobel.tado.client.TadoApiClient
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.specs.StringSpec
import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Tags
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.mockk

class TadoMeterFactoryTest : StringSpec({

  "homeTags creates Tag for home Id" {
    val home = me.homes.first()
    homeTags(home).toList() shouldContainExactlyInAnyOrder Tags.of(
      TadoMeterFactory.TAG_HOME_ID, home.id.toString()
    ).toList()
  }

  "zoneTags creates Tags for home Id and zone Id" {
    val home = me.homes.first()
    val zone = livingRoom
    zoneTags(home, zone).toList() shouldContainExactlyInAnyOrder Tags.of(
      TadoMeterFactory.TAG_HOME_ID, home.id.toString(),
      TadoMeterFactory.TAG_ZONE_ID, zone.id.toString(),
      TadoMeterFactory.TAG_ZONE_NAME, zone.name,
      TadoMeterFactory.TAG_ZONE_TYPE, zone.type.value
    ).toList()
  }

  "createHomeMeters adds all required meters" {
    val meterRegistry = SimpleMeterRegistry()
    val tadoApiClient = mockk<TadoApiClient>()

    val factory = TadoMeterFactory(meterRegistry, tadoApiClient)
    val home = me.homes.first()

    factory.createHomeMeters(home)

    meterRegistry.meters shouldContainMetersExactlyInAnyOrder setOf(
      matching(TadoMeterFactory.SOLAR_INTENSITY_PERCENTAGE, Meter.Type.GAUGE, homeTags(home)),
      matching(TadoMeterFactory.TEMPERATURE_OUTSIDE_CELSIUS, Meter.Type.GAUGE, homeTags(home)),
      matching(TadoMeterFactory.TEMPERATURE_OUTSIDE_FAHRENHEIT, Meter.Type.GAUGE, homeTags(home))
    )
  }

  "createZoneMeters adds all required meters for a Heating Zone" {
    val meterRegistry = SimpleMeterRegistry()
    val tadoApiClient = mockk<TadoApiClient>()

    val factory = TadoMeterFactory(meterRegistry, tadoApiClient)
    val home = me.homes.first()
    val zone = livingRoom

    factory.createZoneMeters(home, listOf(zone))

    meterRegistry.meters shouldContainMetersExactlyInAnyOrder setOf(
      matching(TadoMeterFactory.IS_WINDOW_OPEN, Meter.Type.GAUGE, zoneTags(home, zone)),
      matching(TadoMeterFactory.IS_ZONE_POWERED, Meter.Type.GAUGE, zoneTags(home, zone)),
      matching(TadoMeterFactory.TEMPERATURE_SET_CELSIUS, Meter.Type.GAUGE, zoneTags(home, zone)),
      matching(TadoMeterFactory.TEMPERATURE_SET_FAHRENHEIT, Meter.Type.GAUGE, zoneTags(home, zone)),
      matching(TadoMeterFactory.TEMPERATURE_MEASURED_CELSIUS, Meter.Type.GAUGE, zoneTags(home, zone)),
      matching(TadoMeterFactory.TEMPERATURE_MEASURED_FAHRENHEIT, Meter.Type.GAUGE, zoneTags(home, zone)),
      matching(TadoMeterFactory.HUMIDITY_MEASURED_PERCENTAGE, Meter.Type.GAUGE, zoneTags(home, zone)),
      matching(TadoMeterFactory.HEATING_POWER_PERCENTAGE, Meter.Type.GAUGE, zoneTags(home, zone))
    )
  }

  "createZoneMeters adds all required meters for an Air Conditioning Zone" {
    val meterRegistry = SimpleMeterRegistry()
    val tadoApiClient = mockk<TadoApiClient>()

    val factory = TadoMeterFactory(meterRegistry, tadoApiClient)
    val home = me.homes.first()
    val zone = bedRoom

    factory.createZoneMeters(home, listOf(zone))

    meterRegistry.meters shouldContainMetersExactlyInAnyOrder setOf(
      matching(TadoMeterFactory.IS_WINDOW_OPEN, Meter.Type.GAUGE, zoneTags(home, zone)),
      matching(TadoMeterFactory.IS_ZONE_POWERED, Meter.Type.GAUGE, zoneTags(home, zone)),
      matching(TadoMeterFactory.TEMPERATURE_SET_CELSIUS, Meter.Type.GAUGE, zoneTags(home, zone)),
      matching(TadoMeterFactory.TEMPERATURE_SET_FAHRENHEIT, Meter.Type.GAUGE, zoneTags(home, zone)),
      matching(TadoMeterFactory.TEMPERATURE_MEASURED_CELSIUS, Meter.Type.GAUGE, zoneTags(home, zone)),
      matching(TadoMeterFactory.TEMPERATURE_MEASURED_FAHRENHEIT, Meter.Type.GAUGE, zoneTags(home, zone)),
      matching(TadoMeterFactory.HUMIDITY_MEASURED_PERCENTAGE, Meter.Type.GAUGE, zoneTags(home, zone))
    )
  }

  "createZoneMeters adds all required meters for a Hot Water Zone" {
    val meterRegistry = SimpleMeterRegistry()
    val tadoApiClient = mockk<TadoApiClient>()

    val factory = TadoMeterFactory(meterRegistry, tadoApiClient)
    val home = me.homes.first()
    val zone = hotWaterZone

    factory.createZoneMeters(home, listOf(zone))

    meterRegistry.meters shouldContainMetersExactlyInAnyOrder setOf(
      matching(TadoMeterFactory.IS_ZONE_POWERED, Meter.Type.GAUGE, zoneTags(home, zone))
    )
  }
}) {
  override fun beforeTest(testCase: TestCase) {
    Metrics.globalRegistry.add(SimpleMeterRegistry())
  }

  override fun afterTest(testCase: TestCase, result: TestResult) {
    Metrics.globalRegistry.clear()
  }
}

private infix fun <T : Collection<out Meter>> T.shouldContainMetersExactlyInAnyOrder(matchers: Set<MeterIdMatcher>) =
  map { matching(it) } shouldContainExactlyInAnyOrder matchers

private data class MeterIdMatcher(
  val name: String,
  val type: Meter.Type,
  val tags: List<Tag>
)

private fun matching(name: String, type: Meter.Type, tags: Iterable<Tag>): MeterIdMatcher {
  return MeterIdMatcher(name, type, tags.toList())
}

private fun matching(meter: Meter): MeterIdMatcher =
  matching(meter.id.name.removePrefix(TadoMeterFactory.PREFIX), meter.id.type, meter.id.tags)
