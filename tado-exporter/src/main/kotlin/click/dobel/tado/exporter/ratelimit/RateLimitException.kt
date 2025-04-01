package click.dobel.tado.exporter.ratelimit

class RateLimitException(
  message: String,
  cause: Throwable? = null
) : Exception(message, cause)
