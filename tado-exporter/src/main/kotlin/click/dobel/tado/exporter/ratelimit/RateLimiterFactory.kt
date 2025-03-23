package click.dobel.tado.exporter.ratelimit

import kotlin.time.Duration

typealias RateLimiterFactory = (
  usecase: String,
  interval: Duration
) -> RateLimiter
