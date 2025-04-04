package click.dobel.tado.exporter.ratelimit

import click.dobel.tado.exporter.apiclient.auth.RateLimitException
import java.time.Clock
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration
import kotlin.time.toJavaDuration

class OncePerIntervalRateLimiter(
  private val usecase: String,
  interval: Duration,
  private val clock: Clock = Clock.systemUTC()
) : RateLimiter {
  private val interval = interval.toJavaDuration()
  private val lastExecution = AtomicReference(Instant.MIN)

  override fun <T> executeRateLimited(block: () -> T): T {
    checkRateLimit()
    return try {
      block()
    } finally {
      updateLastExecution()
    }
  }

  private fun checkRateLimit() {
    val noAuthAttemptBefore = lastExecution.get().plus(interval)
    if (now().isBefore(noAuthAttemptBefore))
      throw RateLimitException("$usecase rate limit of 1/${interval.toSeconds()}s exceeded.")
  }

  private fun updateLastExecution() = lastExecution.set(now())

  private fun now() = Instant.now(clock)
}
