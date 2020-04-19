package click.dobel.tado.exporter

import io.micronaut.runtime.Micronaut
import sun.misc.Signal
import kotlin.system.exitProcess

object Application {

  private const val SIGINT = "INT"
  private const val SIGTERM = "TERM"

  @JvmStatic
  fun main(args: Array<String>) {
    Signal.handle(Signal(SIGINT)) { _ -> exitProcess(0) }
    Signal.handle(Signal(SIGTERM)) { _ -> exitProcess(1) }
    Micronaut.build()
      .packages("click.dobel.tado")
      .mainClass(Application.javaClass)
      .start()
  }
}
