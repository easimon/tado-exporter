package click.dobel.tado.metrics

import click.dobel.tado.api.UserHomes
import click.dobel.tado.client.TadoApiClient
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verifySequence

internal class HomeModelRefresherTest : StringSpec({

  "refreshHomeModel adds all zone meters from empty state" {
    val factory = mockk<TadoMeterFactory>()
    val client = mockk<TadoApiClient>()
    val modelRefresher = HomeModelRefresher(factory, client)

    every { client.me() } returns me
    every { client.zones(any()) } returns initialZones

    val createHomeMetersArgSlot = slot<UserHomes>()
    every {
      factory.createHomeMeters(capture(createHomeMetersArgSlot))
    } answers {
      createHomeMetersArgSlot.captured
    }

    every { factory.createZoneMeters(any(), any()) } just runs

    modelRefresher.refreshHomeModel()

    verifySequence {
      client.me()
      factory.createHomeMeters(me.homes.first())
      client.zones(homeId)
      factory.createZoneMeters(me.homes.first(), initialZones.toSet())
    }
  }

  "refreshHomeModel adds only zone meters to previously unknown zones" {
    val factory = mockk<TadoMeterFactory>()
    val client = mockk<TadoApiClient>()
    val modelRefresher = HomeModelRefresher(factory, client)

    every { client.me() } returns me
    every { client.zones(any()) } returns initialZones andThen updatedZones

    val createHomeMetersArgSlot = slot<UserHomes>()
    every {
      factory.createHomeMeters(capture(createHomeMetersArgSlot))
    } answers {
      createHomeMetersArgSlot.captured
    }

    every { factory.createZoneMeters(any(), any()) } just runs

    modelRefresher.refreshHomeModel()
    modelRefresher.refreshHomeModel()

    verifySequence {
      client.me()
      factory.createHomeMeters(me.homes.first())
      client.zones(homeId)
      factory.createZoneMeters(me.homes.first(), initialZones.toSet())
      client.zones(homeId)
      factory.createZoneMeters(me.homes.first(), setOf(bedRoom))
    }
  }
})

