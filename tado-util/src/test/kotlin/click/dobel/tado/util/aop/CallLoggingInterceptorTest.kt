package click.dobel.tado.util.aop

import click.dobel.tado.util.aop.CallLoggingInterceptor.Companion.unmatched
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.aspectj.lang.reflect.CodeSignature
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint
import org.springframework.core.Ordered

internal class CallLoggingInterceptorTest : StringSpec({
  val param1 = "param1"
  val param2 = "param2"
  val paramValue1 = "paramValue1"
  val paramValue2 = "paramValue2"

  val interceptor = CallLoggingInterceptor()
  val contextMock = mockk<MethodInvocationProceedingJoinPoint>()
  val signatureMock = mockk<CodeSignature>()

  "order should be lowest" {
    interceptor.order shouldBe Ordered.LOWEST_PRECEDENCE
  }

  "maps existing parameters to their values" {
    every { signatureMock.parameterNames } returns arrayOf(param1, param2)
    every { contextMock.signature } returns signatureMock
    every { contextMock.args } returns arrayOf(paramValue1, paramValue2)

    interceptor.paramValues(
      contextMock,
      arrayOf(param2, param1)
    ) shouldBe
      arrayOf(paramValue2, paramValue1)
  }

  "maps non-existing parameters to unmatched template" {
    every { signatureMock.parameterNames } returns arrayOf(param1)
    every { contextMock.signature } returns signatureMock
    every { contextMock.args } returns arrayOf(paramValue1)

    interceptor.paramValues(
      contextMock,
      arrayOf(param2, param1)
    ) shouldBe
      arrayOf(unmatched(param2), paramValue1)
  }

  "maps existing null parameters to null" {
    every { signatureMock.parameterNames } returns arrayOf(param1, param2)
    every { contextMock.signature } returns signatureMock
    every { contextMock.args } returns arrayOf(paramValue1, null)

    interceptor.paramValues(
      contextMock,
      arrayOf(param2, param1)
    ) shouldBe
      arrayOf(null, paramValue1)
  }
})
