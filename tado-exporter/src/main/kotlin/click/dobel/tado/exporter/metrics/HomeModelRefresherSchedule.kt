package click.dobel.tado.exporter.metrics

import click.dobel.tado.exporter.apiclient.TadoConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinDuration

@Component
@Profile("!test")
class HomeModelRefresherSchedule(
  private val refresher: HomeModelRefresher,
  private val configuration: TadoConfigurationProperties
) {
  private var lastCheckedAt: Instant = Instant.MIN

  @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
  fun scheduleRefresh() {
    val interval = if (refresher.isInitialized)
      configuration.zoneDiscoveryInterval.toKotlinDuration()
    else 15.seconds

    if (Instant.now() > lastCheckedAt.plusSeconds(interval.inWholeSeconds)) {
      lastCheckedAt = Instant.now()
      refresher.refreshHomeModel()
    }
  }
}
