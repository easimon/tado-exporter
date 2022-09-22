package click.dobel.tado.exporter.metrics

import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class HomeModelRefresherSchedule(
  private val refresher: HomeModelRefresher
) {
  @Scheduled(fixedRateString = "\${tado.zone-discovery-interval}")
  fun scheduleRefresh() {
    refresher.refreshHomeModel()
  }
}
