package click.dobel.tado

import click.dobel.tado.api.HeatingCapabilities
import click.dobel.tado.api.HeatingZoneSetting
import click.dobel.tado.api.HomeInfo
import click.dobel.tado.client.auth.request.TadoAuthLoginRequest
import click.dobel.tado.client.auth.request.TadoAuthRefreshRequest
import click.dobel.tado.client.auth.request.TadoAuthRequest
import click.dobel.tado.client.auth.response.TadoAuthResponse
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.forAll
import io.kotlintest.tables.headers
import io.kotlintest.tables.row
import io.kotlintest.tables.table
import io.micronaut.core.beans.BeanIntrospection

class BeanIntrospectionIntegrationTest : StringSpec({

  "BeanIntrospection for class is available" {
    table(
      headers("Class"),
      row(HomeInfo::class),
      row(HeatingZoneSetting::class),
      row(HeatingCapabilities::class),
      row(TadoAuthRequest::class),
      row(TadoAuthLoginRequest::class),
      row(TadoAuthRefreshRequest::class),
      row(TadoAuthResponse::class)
    ).forAll { cls ->
      BeanIntrospection.getIntrospection(cls.java)
    }
  }

  "BeanIntrospection for class HomeInfo is available" {
    val introspection = BeanIntrospection.getIntrospection(HomeInfo::class.java)
    introspection.propertyNames.toList() shouldContainAll listOf("id", "name", "temperatureUnit")
  }
})
