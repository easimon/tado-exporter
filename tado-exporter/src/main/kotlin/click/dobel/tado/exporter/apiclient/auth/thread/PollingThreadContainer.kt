package click.dobel.tado.exporter.apiclient.auth.thread

import click.dobel.tado.exporter.apiclient.auth.TadoAuthenticationState
import mu.KotlinLogging
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class PollingThreadContainer(
  val name: String,
  interval: Duration,
  immediately: Boolean,
  pollingFunction: () -> TadoAuthenticationState
) {

  companion object {
    private val logger = KotlinLogging.logger { }
  }

  private val resultContainer: AtomicReference<TadoAuthenticationState> = AtomicReference(null)
  val result: TadoAuthenticationState? get() = resultContainer.get()

  private val pollingLoop = Runnable {
    val sanitizedInterval: Duration = minOf(interval, 5.milliseconds)

    while (result == null) {
      sleep(sanitizedInterval.inWholeMilliseconds)
      try {
        resultContainer.set(pollingFunction())
      } catch (transient: TransientPollingError) {
        if (logger.isDebugEnabled) {
          logger.debug(transient) { "Polling failed, will retry in $sanitizedInterval." }
        } else {
          logger.info { "Polling failed: ${transient.message}, will retry in $sanitizedInterval." }
        }
      } catch (ex: Exception) {
        resultContainer.set(TadoAuthenticationState.Error(ex))
      }
    }
  }

  private val pollingThread = Thread(pollingLoop, name)

  init {
    if (immediately) runCatching {
      resultContainer.set(pollingFunction())
    }

    if (result == null) pollingThread.start()
  }
}
