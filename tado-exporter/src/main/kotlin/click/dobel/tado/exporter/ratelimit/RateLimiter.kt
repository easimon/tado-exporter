package click.dobel.tado.exporter.ratelimit

interface RateLimiter {
  fun <T> executeRateLimited(block: () -> T): T
}
