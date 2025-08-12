package click.dobel.tado.exporter.apiclient.auth.thread

open class TransientPollingError(
  message: String,
  cause: Throwable? = null
) : Exception(
  message, cause
) {
  constructor(cause: Throwable? = null) : this(cause?.message ?: "No message", cause)
}
