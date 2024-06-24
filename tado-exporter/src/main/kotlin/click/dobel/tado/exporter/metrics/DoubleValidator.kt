package click.dobel.tado.exporter.metrics

class DoubleValidator(private val invalidValue: Double) {
  private val blockInfinite = invalidValue.isInfinite()
  private val blockNaNs = invalidValue.isNaN()

  fun isValid(value: Double): Boolean {
    // special cases for NaN and Infinity: can't be compared using equality.
    return !(blockInfinite && value.isInfinite()) &&
      !(blockNaNs && value.isNaN()) &&
      (invalidValue != value)
  }
}
