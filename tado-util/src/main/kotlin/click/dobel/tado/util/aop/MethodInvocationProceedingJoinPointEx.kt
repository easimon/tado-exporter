package click.dobel.tado.util.aop

import org.aspectj.lang.reflect.CodeSignature
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint

fun MethodInvocationProceedingJoinPoint.parameterValueMap(): Map<String, Any?> {
  val parameterNames = (this.signature as CodeSignature).parameterNames
  val parameterValues = this.args
  return parameterNames.zip(parameterValues).toMap()
}
