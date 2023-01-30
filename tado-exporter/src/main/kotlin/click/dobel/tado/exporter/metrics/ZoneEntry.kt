package click.dobel.tado.exporter.metrics

import click.dobel.tado.api.TadoSystemType
import click.dobel.tado.api.Zone

data class ZoneEntry(
  val id: Int,
  val name: String,
  val type: TadoSystemType
) {
  constructor(zone: Zone) : this(zone.id, zone.name, zone.type)
}
