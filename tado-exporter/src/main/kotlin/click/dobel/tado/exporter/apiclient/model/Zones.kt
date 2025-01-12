package click.dobel.tado.exporter.apiclient.model

import click.dobel.tado.api.Zone
import click.dobel.tado.exporter.metrics.ZoneEntry
import com.fasterxml.jackson.annotation.JsonCreator

class Zones @JsonCreator constructor(zones: Collection<Zone>) : List<Zone> by ArrayList(zones)

fun Collection<Zone>.toEntrySet() = this.map(::ZoneEntry).toSet()
