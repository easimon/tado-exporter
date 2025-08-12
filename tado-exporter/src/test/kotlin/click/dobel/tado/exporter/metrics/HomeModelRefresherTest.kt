package click.dobel.tado.exporter.metrics

import click.dobel.tado.api.UserHomes
import click.dobel.tado.exporter.apiclient.TadoApiClient
import click.dobel.tado.exporter.apiclient.model.toEntrySet
import io.kotest.core.spec.style.StringSpec
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

    every { client.isAuthenticated } returns true
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
      client.isAuthenticated
      client.me()
      factory.createHomeMeters(me.homes.first())
      client.zones(homeId)
      factory.createZoneMeters(me.homes.first(), initialZones.toEntrySet())
    }
  }

  "refreshHomeModel adds only zone meters to previously unknown zones" {
    val factory = mockk<TadoMeterFactory>()
    val client = mockk<TadoApiClient>()
    val modelRefresher = HomeModelRefresher(factory, client)

    every { client.isAuthenticated } returns true
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
      client.isAuthenticated
      client.me()
      factory.createHomeMeters(me.homes.first())
      client.zones(homeId)
      factory.createZoneMeters(me.homes.first(), initialZones.toEntrySet())
      client.zones(homeId)
      factory.createZoneMeters(me.homes.first(), setOf(ZoneEntry(bedRoom)))
    }
  }

  "refreshHomeModel clears model on network failures" {
    val factory = mockk<TadoMeterFactory>()
    val client = mockk<TadoApiClient>()
    val modelRefresher = HomeModelRefresher(factory, client)

    every { client.isAuthenticated } returns true
    every { client.me() } returns me
    every { client.zones(any()) } returns initialZones andThenThrows RuntimeException("Something went wrong")

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
      client.isAuthenticated
      client.me()
      factory.createHomeMeters(me.homes.first())
      client.zones(homeId)
      factory.createZoneMeters(me.homes.first(), initialZones.toEntrySet())
      client.zones(homeId)
      factory.createZoneMeters(me.homes.first(), emptyZones.toEntrySet())
    }
  }
})

