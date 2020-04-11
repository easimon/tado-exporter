package click.dobel.tado.micronaut

import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.core.annotation.Introspected
import io.micronaut.inject.annotation.NamedAnnotationMapper
import io.micronaut.inject.visitor.VisitorContext

class GeneratedAnnotationMapper : NamedAnnotationMapper {
  override fun getName(): String {
    return "javax.annotation.Generated"
  }

  override fun map(annotation: AnnotationValue<Annotation>, visitorContext: VisitorContext?): MutableList<AnnotationValue<*>> {
    return mutableListOf(
      AnnotationValue
        .builder(Introspected::class.java)
        .build()
    )
  }
}
