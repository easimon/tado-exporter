package click.dobel.tado.util.aop

import click.dobel.tado.util.logger
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint
import org.springframework.core.Ordered

@Aspect
class CallLoggingInterceptor : Ordered {
  companion object {
    internal fun unmatched(param: String) = "<unmatched param '${param}'>"
  }

  @Around("@annotation(logged)")
  fun log(context: ProceedingJoinPoint, logged: Logged): Any? {
    if (context !is MethodInvocationProceedingJoinPoint) {
      context.target?.logger()?.warn("Incorrect usage of @Logged, target must be a method.")
    } else {
      val message = logged.message
      val paramNames = logged.params
      val params = paramValues(context, paramNames)
      context.target?.logger()?.info(message, *params)
    }
    return context.proceed()
  }

  internal fun paramValues(context: MethodInvocationProceedingJoinPoint, params: Array<String>): Array<Any?> {
    val arguments = context.parameterValueMap()

    return params.map { param ->
      if (arguments.containsKey(param)) {
        arguments[param]
      } else {
        unmatched(param)
      }
    }.toTypedArray()
  }

  override fun getOrder() = Ordered.LOWEST_PRECEDENCE
}

