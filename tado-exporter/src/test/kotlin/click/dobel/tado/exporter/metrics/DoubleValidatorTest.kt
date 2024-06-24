package click.dobel.tado.exporter.metrics

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class DoubleValidatorTest : FreeSpec({

  "DoubleValidator" - {
    "configured with NaN as invalid value" - {
      val validator = DoubleValidator(Double.NaN)

      "should not accept NaN" { validator.isValid(Double.NaN) shouldBe false }
      "should accept 0.0" { validator.isValid(0.0) shouldBe true }
      "should accept 5.0" { validator.isValid(5.0) shouldBe true }
      "should accept Infinity" { validator.isValid(Double.POSITIVE_INFINITY) shouldBe true }
      "should accept -Infinity" { validator.isValid(Double.NEGATIVE_INFINITY) shouldBe true }
    }

    "configured with 0.0 as invalid value" - {
      val validator = DoubleValidator(0.0)

      "should not accept 0.0" { validator.isValid(0.0) shouldBe false }
      "should accept 5.0" { validator.isValid(5.0) shouldBe true }
      "should accept NaN" { validator.isValid(Double.NaN) shouldBe true }
      "should accept Infinity" { validator.isValid(Double.POSITIVE_INFINITY) shouldBe true }
      "should accept -Infinity" { validator.isValid(Double.NEGATIVE_INFINITY) shouldBe true }
    }
    "configured with Infinity as invalid value" - {
      val validator = DoubleValidator(Double.NEGATIVE_INFINITY)

      "should not accept Infinity" { validator.isValid(Double.POSITIVE_INFINITY) shouldBe false }
      "should not accept -Infinity" { validator.isValid(Double.NEGATIVE_INFINITY) shouldBe false }
      "should accept 0.0" { validator.isValid(0.0) shouldBe true }
      "should accept 5.0" { validator.isValid(5.0) shouldBe true }
      "should accept NaN" { validator.isValid(Double.NaN) shouldBe true }
    }
  }
})
