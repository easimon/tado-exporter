package click.dobel.tado.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

fun Any.logger() = logger(this::class)

fun logger(forClass: KClass<*>): Logger {
  return LoggerFactory.getLogger(unwrapCompanion(forClass))
}

private fun unwrapCompanion(fromClass: KClass<*>): Class<*> {
  return if (fromClass.isCompanion) {
    fromClass.java.enclosingClass
  } else {
    fromClass.java
  }
}
