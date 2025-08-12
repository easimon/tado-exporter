package click.dobel.tado.exporter.metrics

import click.dobel.tado.api.TadoSystemType
import click.dobel.tado.api.User
import click.dobel.tado.api.UserHomes
import click.dobel.tado.api.Zone
import click.dobel.tado.exporter.apiclient.model.Zones

internal const val userId = "123"
internal const val homeId = 42
internal val me = createMe()
internal val hotWaterZone = createZone(0, TadoSystemType.HOT_WATER, "Hot Water")
internal val livingRoom = createZone(1, TadoSystemType.HEATING, "LivingRoom")
internal val bedRoom = createZone(0, TadoSystemType.AIR_CONDITIONING, "Bedroom")
internal val initialZones = Zones(listOf(hotWaterZone, livingRoom))
internal val updatedZones = Zones(listOf(hotWaterZone, bedRoom))
internal val emptyZones = Zones(emptyList())

private fun createMe(): User {
  return User()
    .id(userId)
    .homes(createHomes())
}

private fun createHomes(): MutableList<UserHomes> {
  return mutableListOf(
    UserHomes()
      .id(homeId)
      .name("Home")
  )
}

private fun createZone(id: Int, type: TadoSystemType, name: String): Zone {
  return TestZone(id).type(type).name(name)
}

private class TestZone(
  private val fakeId: Int
) : Zone() {
  override fun getId(): Int = fakeId
}
