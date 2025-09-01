package box.tapsi.libs.metrics.core.annotations

/**
 * Annotation for capturing reactive metrics by wrapping reactive publishers (e.g., Mono or Flux).
 * Used to define custom metric names and additional tags to be associated with the metric.
 * Supports reactive method instrumentation and can be applied at the class or method level.
 *
 * @property name The name of the metric to be recorded. If left empty, a default metric name may be used.
 * @property extraTags An array of key-value pairs representing additional tags to be associated with the metric,
 *                    where even-indexed elements are keys, and odd-indexed elements are their corresponding values.
 */
@Target(
  AnnotationTarget.FUNCTION,
  AnnotationTarget.TYPE,
  AnnotationTarget.CLASS,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class ReactiveTimed(
  val name: String = "",
  val extraTags: Array<String> = []
)

