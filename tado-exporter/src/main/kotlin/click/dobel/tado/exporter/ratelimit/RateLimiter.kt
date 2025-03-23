package click.dobel.tado.exporter.ratelimit

@Suppress("kotlin:S6517") // SAM interfaces cannot declare type parameters
interface RateLimiter {
  fun <T> executeRateLimited(block: () -> T): T
}
