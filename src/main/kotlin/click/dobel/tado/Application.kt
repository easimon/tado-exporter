package click.dobel.tado

import io.micronaut.runtime.Micronaut

object Application {

  @JvmStatic
  fun main(args: Array<String>) {
    Micronaut.build()
      .packages("click.dobel.tado")
      .mainClass(Application.javaClass)
      .start()
  }
}
