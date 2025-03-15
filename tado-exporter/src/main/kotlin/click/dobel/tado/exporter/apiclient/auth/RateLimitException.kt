package click.dobel.tado.exporter.apiclient.auth

class RateLimitException(
  message: String,
  cause: Throwable? = null
) : Exception(message, cause)
