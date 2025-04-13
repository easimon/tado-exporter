package click.dobel.tado.exporter.ratelimit

import click.dobel.tado.exporter.apiclient.auth.thread.TransientPollingError

class RateLimitException(
  message: String,
  cause: Throwable? = null
) : TransientPollingError(message, cause)
