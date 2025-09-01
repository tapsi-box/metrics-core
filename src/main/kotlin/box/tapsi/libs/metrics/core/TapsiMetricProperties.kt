package box.tapsi.libs.metrics.core

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.Ordered

@ConfigurationProperties("box.libs.metrics")
data class TapsiMetricProperties(
  val reactiveTimed: ReactiveTimed = ReactiveTimed()
) {
  /**
   * Represents configuration settings for reactive metrics timing.
   *
   * This class is utilized to configure the default behavior for reactive timing in the metrics system,
   * enabling control over aspects such as execution order, default tags, and the inclusion of class
   * and method names in metric data.
   *
   * @property order Specifies the execution order of metrics instrumentation. Defaults to the lowest precedence.
   * @property defaultTags A map of default tags to be added to metrics.
   * These tags serve as key-value pairs for additional metadata.
   * @property includeClassName Indicates whether the class name should be included as a tag in the metrics.
   * @property includeMethodName Indicates whether the method name should be included as a tag in the metrics.
   */
  data class ReactiveTimed(
    val order: Int = Ordered.LOWEST_PRECEDENCE,
    val defaultTags: Map<String, String> = emptyMap(),
    val includeClassName: Boolean = true,
    val includeMethodName: Boolean = true,
  )
}
