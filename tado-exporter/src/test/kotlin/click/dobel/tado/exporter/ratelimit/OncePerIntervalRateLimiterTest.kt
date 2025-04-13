package click.dobel.tado.exporter.ratelimit

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.Clock
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class OncePerIntervalRateLimiterTest : FreeSpec({

  val clock = mockk<Clock>()

  lateinit var testTime: Instant
  lateinit var rateLimiter: OncePerIntervalRateLimiter

  val interval = 1.minutes

  fun execute(): Boolean = true
  fun throwing(): Boolean = error("test")

  beforeEach {
    testTime = Instant.now()
    every { clock.instant() } answers { testTime }

    rateLimiter = OncePerIntervalRateLimiter("Test", interval, clock)
  }

  "should allow execution once" {
    rateLimiter.executeRateLimited { execute() } shouldBe true
  }

  "should disallow second execution within the interval" {
    rateLimiter.executeRateLimited { execute() } shouldBe true
    shouldThrow<RateLimitException> { rateLimiter.executeRateLimited { execute() } }
  }

  "should disallow further executions within the interval" {
    rateLimiter.executeRateLimited { execute() } shouldBe true
    shouldThrow<RateLimitException> { rateLimiter.executeRateLimited { execute() } }
    shouldThrow<RateLimitException> { rateLimiter.executeRateLimited { execute() } }
  }

  "should disallow second execution close within the interval" {
    rateLimiter.executeRateLimited { execute() } shouldBe true

    testTime += interval.toJavaDuration().minusMillis(1)

    shouldThrow<RateLimitException> { rateLimiter.executeRateLimited { execute() } }
  }

  "should count throwing executions" {
    shouldThrow<IllegalStateException> { rateLimiter.executeRateLimited { throwing() } }
    shouldThrow<RateLimitException> { rateLimiter.executeRateLimited { throwing() } }
  }

  "should allow second execution exactly after the interval" {
    rateLimiter.executeRateLimited { execute() } shouldBe true

    testTime += interval.toJavaDuration()

    rateLimiter.executeRateLimited { execute() } shouldBe true
  }
})
