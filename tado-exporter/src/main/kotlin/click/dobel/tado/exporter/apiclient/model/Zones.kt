package click.dobel.tado.exporter.apiclient.model

import click.dobel.tado.api.Zone
import click.dobel.tado.exporter.metrics.ZoneEntry

class Zones(zones: Collection<Zone>) : List<Zone> by ArrayList(zones)

fun Collection<Zone>.toEntrySet() = this.map(::ZoneEntry).toSet()
