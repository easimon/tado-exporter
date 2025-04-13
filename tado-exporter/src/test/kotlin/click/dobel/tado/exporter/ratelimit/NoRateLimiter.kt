package click.dobel.tado.exporter.ratelimit

import kotlin.time.Duration

class NoRateLimiter(usecase: String, interval: Duration) : RateLimiter {
  override fun <T> executeRateLimited(block: () -> T): T = block()
}
