package click.dobel.tado.util.aop

import click.dobel.tado.util.logger
import click.dobel.tado.util.stringValue
import click.dobel.tado.util.stringValues
import io.micronaut.aop.InterceptPhase
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import javax.inject.Singleton

@Singleton
class CallLoggingInterceptor : MethodInterceptor<Any, Any> {
  companion object {
    private const val MEMBER_MESSAGE = "message"
    private const val MEMBER_PARAMS = "params"

    internal fun unmatched(param: String) = "<unmatched param '${param}'>"
  }

  override fun getOrder() = InterceptPhase.TRACE.position;

  override fun intercept(context: MethodInvocationContext<Any, Any>): Any {
    val message = context.stringValue(Logged::class, MEMBER_MESSAGE)
    val paramNames = context.stringValues(Logged::class, MEMBER_PARAMS)
    val params = paramValues(context, paramNames)

    context.target.logger().info(message, *params)

    return context.proceed()
  }

  internal fun paramValues(context: MethodInvocationContext<Any, Any>, params: Array<String>): Array<Any?> {
    val arguments = context.parameterValueMap
    return params.map { param ->
      if (arguments.containsKey(param)) {
        arguments[param]
      } else {
        unmatched(param)
      }
    }.toTypedArray()
  }
}

