package click.dobel.tado.util

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.types.shouldBeSameInstanceAs

internal class LoggersKtTest : StringSpec({
  "Returns same Logger for Class and Companion" {
    logger(LoggerTestClass::class) shouldBeSameInstanceAs logger(LoggerTestClass.Companion::class)
  }

  "Returns Logger for the enclosing class when called without parameters" {
    LoggerTestClass.LOGGER shouldBeSameInstanceAs logger(LoggerTestClass::class)
  }
})

internal class LoggerTestClass {
  companion object {
    val LOGGER = logger()
  }
}
