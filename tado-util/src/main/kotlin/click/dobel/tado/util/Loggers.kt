package click.dobel.tado.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

private const val COMPANION_SUFFIX = "\$Companion"
private const val INTERCEPTED_SUFFIX = "\$Intercepted"

private val SUFFIXES = listOf(COMPANION_SUFFIX, INTERCEPTED_SUFFIX)

fun Any.logger() = logger(this::class)

fun logger(forClass: KClass<*>): Logger {
  return LoggerFactory.getLogger(removeSuffixes(forClass))
}

private fun removeSuffixes(fromClass: KClass<*>): String {
  var className: String = fromClass.java.name

  SUFFIXES.forEach { suffix ->
    className = className.removeSuffix(suffix)
  }

  return className
}

@Suppress("UNUSED")
private fun unwrapCompanion(fromClass: KClass<*>): Class<*> {
  // Needs reflection, bad for native images
  return if (fromClass.isCompanion) {
    fromClass.java.enclosingClass
  } else {
    fromClass.java
  }
}
