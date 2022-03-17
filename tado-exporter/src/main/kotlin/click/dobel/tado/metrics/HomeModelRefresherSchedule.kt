package click.dobel.tado.metrics

import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton

@Singleton
@Requires(notEnv = ["test"])
class HomeModelRefresherSchedule(
  private val refresher: HomeModelRefresher
) {
  @Scheduled(fixedRate = "\${tado.zone-discovery-interval}")
  fun scheduleRefresh() {
    refresher.refreshHomeModel()
  }
}
