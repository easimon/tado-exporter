package click.dobel.tado.exporter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TadoExporterApplication

fun main(args: Array<String>) {
  runApplication<TadoExporterApplication>(*args)
}
