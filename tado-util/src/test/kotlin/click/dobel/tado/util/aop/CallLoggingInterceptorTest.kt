package click.dobel.tado.util.aop

import click.dobel.tado.util.aop.CallLoggingInterceptor.Companion.unmatched
import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.matchers.numerics.shouldBeLessThan
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.micronaut.aop.InterceptPhase
import io.micronaut.aop.MethodInvocationContext
import io.mockk.every
import io.mockk.mockk

internal class CallLoggingInterceptorTest : StringSpec({
  val param1 = "param1"
  val param2 = "param2"
  val paramValue1 = "paramValue1"
  val paramValue2 = "paramValue2"

  val interceptor = CallLoggingInterceptor()
  val contextMock = mockk<MethodInvocationContext<Any, Any>>()

  "order should be after CACHE" {
    interceptor.order shouldBeGreaterThan InterceptPhase.CACHE.position
  }

  "order should be before RETRY" {
    interceptor.order shouldBeLessThan InterceptPhase.RETRY.position
  }

  "maps existing parameters to their values" {
    every {
      contextMock.parameterValueMap
    } returns mapOf(
      param1 to paramValue1,
      param2 to paramValue2
    )

    interceptor.paramValues(
      contextMock,
      arrayOf(param2, param1)
    ) shouldBe
      arrayOf(paramValue2, paramValue1)
  }

  "maps non-existing parameters to unmatched template" {
    every {
      contextMock.parameterValueMap
    } returns mapOf(
      param1 to paramValue1
    )

    interceptor.paramValues(
      contextMock,
      arrayOf(param2, param1)
    ) shouldBe
      arrayOf(unmatched(param2), paramValue1)
  }

  "maps existing null parameters to null" {
    every {
      contextMock.parameterValueMap
    } returns mapOf(
      param1 to paramValue1,
      param2 to null
    )

    interceptor.paramValues(
      contextMock,
      arrayOf(param2, param1)
    ) shouldBe
      arrayOf(null, paramValue1)
  }
})
