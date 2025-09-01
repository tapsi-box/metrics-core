package box.tapsi.metrics.core

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.Ordered

@ConfigurationProperties("tapsi.metrics")
data class TapsiMetricProperties(
  val reactiveTimed: ReactiveTimed = ReactiveTimed()
) {
  data class ReactiveTimed(
    val order: Int = Ordered.LOWEST_PRECEDENCE,
    val defaultTags: Map<String, String> = emptyMap(),
    val includeClassName: Boolean = true,
    val includeMethodName: Boolean = true,
  )
}
