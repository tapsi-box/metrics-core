package box.tapsi.metrics.core.annotations

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

