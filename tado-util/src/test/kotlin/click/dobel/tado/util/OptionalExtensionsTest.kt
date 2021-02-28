package click.dobel.tado.util

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.Optional

internal class OptionalExtensionsTest : StringSpec({
  "returns value for non-null optional" {
    Optional.of("Something").orNull() shouldBe "Something"
  }
  "returns value for empty optional" {
    Optional.empty<String>().orNull() shouldBe null
  }
})
