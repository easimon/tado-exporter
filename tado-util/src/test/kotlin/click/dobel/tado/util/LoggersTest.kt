package click.dobel.tado.util

import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.specs.StringSpec

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
