package click.dobel.tado.exporter.metrics

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class ValueFilteringCollectorRegistryTest : FreeSpec({

  "ValueFilteringCollectorRegistry" - {

    "configured with NaN as invalid value" - {
      val registry = ValueFilteringCollectorRegistry(Double.NaN)

      "should not accept NaN" { registry.isValid(Double.NaN) shouldBe false }
      "should accept 0.0" { registry.isValid(0.0) shouldBe true }
      "should accept 5.0" { registry.isValid(5.0) shouldBe true }
      "should accept Infinity" { registry.isValid(Double.POSITIVE_INFINITY) shouldBe true }
      "should accept -Infinity" { registry.isValid(Double.NEGATIVE_INFINITY) shouldBe true }
    }

    "configured with 0.0 as invalid value" - {
      val registry = ValueFilteringCollectorRegistry(0.0)

      "should not accept 0.0" { registry.isValid(0.0) shouldBe false }
      "should accept 5.0" { registry.isValid(5.0) shouldBe true }
      "should accept NaN" { registry.isValid(Double.NaN) shouldBe true }
      "should accept Infinity" { registry.isValid(Double.POSITIVE_INFINITY) shouldBe true }
      "should accept -Infinity" { registry.isValid(Double.NEGATIVE_INFINITY) shouldBe true }
    }
    "configured with Infinity as invalid value" - {
      val registry = ValueFilteringCollectorRegistry(Double.NEGATIVE_INFINITY)

      "should not accept Infinity" { registry.isValid(Double.POSITIVE_INFINITY) shouldBe false }
      "should not accept -Infinity" { registry.isValid(Double.NEGATIVE_INFINITY) shouldBe false }
      "should accept 0.0" { registry.isValid(0.0) shouldBe true }
      "should accept 5.0" { registry.isValid(5.0) shouldBe true }
      "should accept NaN" { registry.isValid(Double.NaN) shouldBe true }
    }
  }
})
