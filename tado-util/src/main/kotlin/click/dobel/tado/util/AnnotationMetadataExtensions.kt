package click.dobel.tado.util

import io.micronaut.core.annotation.AnnotationMetadata
import kotlin.reflect.KClass

fun AnnotationMetadata.stringValue(
  clazz: KClass<out Annotation>,
  member: String
): String? =
  stringValue(clazz.java, member).orNull()

fun AnnotationMetadata.stringValues(
  clazz: KClass<out Annotation>,
  member: String
): Array<String> =
  stringValues(clazz.java, member)
