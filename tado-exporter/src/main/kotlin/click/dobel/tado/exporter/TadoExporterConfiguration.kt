package click.dobel.tado.exporter

import click.dobel.tado.api.HomeInfo
import click.dobel.tado.api.User
import click.dobel.tado.api.WeatherReport
import click.dobel.tado.api.Zone
import click.dobel.tado.api.ZoneState
import click.dobel.tado.exporter.apiclient.TadoConfigurationProperties
import click.dobel.tado.exporter.metrics.TadoMeterFactory
import click.dobel.tado.util.aop.CallLoggingInterceptor
import com.github.benmanes.caffeine.cache.Caffeine
import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.config.MeterFilter
import io.micrometer.core.instrument.config.MeterFilterReply
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties(TadoConfigurationProperties::class)
class TadoExporterConfiguration {

  companion object {
    // TODO collect automatically
    private val API_CLASSES = listOf(
      User::class,
      HomeInfo::class,
      Zone::class,
      ZoneState::class,
      WeatherReport::class
    )
  }

  fun cacheNames(): Array<String> = API_CLASSES.map { it.simpleName!! }.toTypedArray()

  // cache is only used to cache shelly http responses during a single metrics call
  // any reasonably short value (shorter than scrape interval) will so
  @Bean
  fun caffeineConfig(): Caffeine<Any, Any> =
    Caffeine
      .newBuilder()
      .expireAfterWrite(10, TimeUnit.SECONDS)

  @Bean
  fun cacheManager(caffeineConfig: Caffeine<Any, Any>): CacheManager {
    val manager = CaffeineCacheManager(*cacheNames())
    manager.setCaffeine(caffeineConfig)
    return manager
  }

  @Bean
  fun meterFilter(): MeterFilter? {
    return object : MeterFilter {
      override fun accept(id: Meter.Id): MeterFilterReply {
        return if (id.name.startsWith(TadoMeterFactory.PREFIX)) {
          MeterFilterReply.ACCEPT
        } else {
          MeterFilterReply.DENY
        }
      }
    }
  }

  @Bean
  fun aopLogger() = CallLoggingInterceptor()


}
