package click.dobel.tado.exporter

import io.micronaut.runtime.Micronaut.build
import sun.misc.Signal
import kotlin.system.exitProcess

private val SHUTDOWN_SIGNALS = listOf("INT", "TERM")

fun main(args: Array<String>) {
  SHUTDOWN_SIGNALS.forEach { signal ->
    Signal.handle(Signal(signal)) { exitProcess(0) }
  }
  build()
    .args(*args)
    .packages("click.dobel.tado")
    .start()
}
