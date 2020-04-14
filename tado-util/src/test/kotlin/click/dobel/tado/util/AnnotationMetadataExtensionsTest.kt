package click.dobel.tado.util

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.micronaut.core.annotation.AnnotationMetadata
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import java.util.Optional

internal class AnnotationMetadataExtensionsTest : StringSpec({

  "stringValue translates to java class" {
    val annotationName = "value"
    val annotationValue = "test"

    val metadata = mockk<AnnotationMetadata>()

    every {
      metadata.stringValue(ofType<Class<out TestAnnotation>>(), any())
    } returns Optional.of(annotationValue)

    metadata.stringValue(TestAnnotation::class, annotationName) shouldBe
      annotationValue

    verifySequence {
      metadata.stringValue(TestAnnotation::class.java, annotationName)
    }
  }

  "stringValues translates to java class" {
    val annotationName = "values"
    val annotationValue = arrayOf("test", "test2")

    val metadata = mockk<AnnotationMetadata>()

    every {
      metadata.stringValues(ofType<Class<out TestAnnotation>>(), any())
    } returns annotationValue

    metadata.stringValues(TestAnnotation::class, annotationName) shouldBe
      annotationValue

    verifySequence {
      metadata.stringValues(TestAnnotation::class.java, annotationName)
    }
  }
})

internal annotation class TestAnnotation
