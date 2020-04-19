package click.dobel.tado.exporter

import io.micronaut.runtime.Micronaut
import sun.misc.Signal
import kotlin.system.exitProcess

object Application {

  private val SHUTDOWN_SIGNALS = listOf("INT", "TERM")

  @JvmStatic
  fun main(args: Array<String>) {
    SHUTDOWN_SIGNALS.forEach { signal ->
      Signal.handle(Signal(signal)) { _ -> exitProcess(0) }
    }
    Micronaut.build()
      .packages("click.dobel.tado")
      .mainClass(Application.javaClass)
      .start()
  }
}
