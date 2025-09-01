package box.tapsi.libs.metrics.core

/**
 * Represents a metric's identifier used in the metrics system.
 * Provides a uniform naming convention for metrics by exposing the name of the meter as a property.
 * This interface is typically implemented by classes that define specific metric names to be used
 * across the application in conjunction with the MeterRegistry or related services.
 */
interface MeterName {
  val meterName: String
}
