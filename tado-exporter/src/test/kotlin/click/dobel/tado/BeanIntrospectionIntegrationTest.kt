package click.dobel.tado

import click.dobel.tado.api.HomeInfo
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.StringSpec
import io.micronaut.core.beans.BeanIntrospection

class BeanIntrospectionIntegrationTest : StringSpec({

  "BeanIntrospection for class HomeInfo is available" {
    val introspection = BeanIntrospection.getIntrospection(HomeInfo::class.java)
    introspection.propertyNames.toList() shouldContainAll listOf("id", "name", "temperatureUnit")
  }
})