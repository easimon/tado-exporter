package click.dobel.tado.exporter.test

import click.dobel.tado.exporter.TadoExporterApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.lang.annotation.Inherited

@Inherited
@SpringBootTest(classes = [TadoExporterApplication::class])
@ActiveProfiles("test")
annotation class IntegrationTest
