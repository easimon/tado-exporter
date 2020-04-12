package click.dobel.tado

import io.micronaut.core.annotation.Introspected
import javax.inject.Singleton
import javax.xml.bind.annotation.XmlRootElement

@Singleton
@Introspected(packages = ["click.dobel.tado.api"], includedAnnotations = [XmlRootElement::class])
class ApplicationConfiguration

