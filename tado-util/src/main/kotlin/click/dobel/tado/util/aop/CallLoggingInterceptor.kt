package click.dobel.tado.util.aop

import click.dobel.tado.util.logger
import io.micronaut.aop.InterceptPhase
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import javax.inject.Singleton
import kotlin.reflect.KClass

@Singleton
class CallLoggingInterceptor : MethodInterceptor<Any, Any> {
  companion object {
    const val MEMBER_MESSAGE = "message"
    const val MEMBER_PARAMS = "params"
  }

  override fun getOrder(): Int {
    return InterceptPhase.TRACE.position;
  }

  override fun intercept(context: MethodInvocationContext<Any, Any>): Any {
    val message = context.stringValue(Logged::class, MEMBER_MESSAGE)
    val paramNames = context.stringValues(Logged::class, MEMBER_PARAMS)
    val params = paramValues(context, paramNames)

    logger(context.target::class)
      .info(message, *params)
    return context.proceed()
  }

  private fun paramValues(context: MethodInvocationContext<Any, Any>, params: Array<String>): Array<Any?> {
    val arguments = context.parameterValueMap
    return params.map { param ->
      if (arguments.containsKey(param)) {
        arguments[param]
      } else {
        "<unmatched param '${param}'>"
      }
    }.toTypedArray()
  }
}

private fun MethodInvocationContext<*, *>.stringValue(
  clazz: KClass<out Annotation>,
  member: String
): String =
  stringValue(clazz.java, member).get()

private fun MethodInvocationContext<*, *>.stringValues(
  clazz: KClass<out Annotation>,
  member: String
): Array<String> =
  stringValues(clazz.java, member)

