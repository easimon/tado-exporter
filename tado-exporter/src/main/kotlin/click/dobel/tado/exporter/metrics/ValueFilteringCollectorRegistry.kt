package click.dobel.tado.exporter.metrics

import io.prometheus.client.Collector.MetricFamilySamples
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Predicate
import java.util.Enumeration

class ValueFilteringCollectorRegistry(
  private val blockedValue: Double,
  autoDescribe: Boolean = false,
) : CollectorRegistry(autoDescribe) {

  private val blockNaNs = blockedValue.isNaN()
  private val blockInfinite = blockedValue.isInfinite()
  internal val isValid: (Double) -> Boolean = {
    // special cases for NaN and Infinity: can't be compared using equality.
    !(blockInfinite && it.isInfinite()) &&
      !(blockNaNs && it.isNaN()) &&
      (blockedValue != it)
  }

  override fun metricFamilySamples(): Enumeration<MetricFamilySamples> {
    return super.metricFamilySamples().filterValidValues()
  }

  override fun filteredMetricFamilySamples(includedNames: MutableSet<String>?): Enumeration<MetricFamilySamples> {
    return super.filteredMetricFamilySamples(includedNames).filterValidValues()
  }

  override fun filteredMetricFamilySamples(sampleNameFilter: Predicate<String>?): Enumeration<MetricFamilySamples> {
    return super.filteredMetricFamilySamples(sampleNameFilter).filterValidValues()
  }

  private fun Enumeration<MetricFamilySamples>.filterValidValues(): Enumeration<MetricFamilySamples> {
    val iterator = asSequence()
      .map { MetricFamilySamples(it.name, it.unit, it.type, it.help, it.samples.filterValid()) }
      .filterNonEmpty()
      .iterator()

    return object : Enumeration<MetricFamilySamples> {
      override fun hasMoreElements(): Boolean {
        return iterator.hasNext()
      }

      override fun nextElement(): MetricFamilySamples {
        return iterator.next()
      }
    }
  }

  private fun Sequence<MetricFamilySamples>.filterNonEmpty(): Sequence<MetricFamilySamples> {
    return this.filter {
      it.samples.isNotEmpty()
    }
  }

  private fun List<MetricFamilySamples.Sample>.filterValid(): List<MetricFamilySamples.Sample> {
    return this.filter {
      isValid(it.value)
    }
  }
}
