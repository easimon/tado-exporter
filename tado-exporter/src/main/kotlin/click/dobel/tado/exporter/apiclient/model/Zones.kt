package click.dobel.tado.exporter.apiclient.model

import click.dobel.tado.api.Zone

class Zones(zones: Collection<Zone>) : List<Zone> by ArrayList(zones)
