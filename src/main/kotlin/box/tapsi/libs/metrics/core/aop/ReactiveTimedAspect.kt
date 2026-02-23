package box.tapsi.libs.metrics.core.aop

import box.tapsi.libs.metrics.core.TapsiMetricProperties
import box.tapsi.libs.metrics.core.annotations.ReactiveTimed
import box.tapsi.libs.metrics.core.services.MeterRegistryService
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.reactivestreams.Publisher
import org.springframework.aop.support.AopUtils
import org.springframework.core.Ordered
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.reflect.Method

/**
 * Aspect for managing reactive timing metrics using annotations.
 * This class intercepts methods annotated with `@ReactiveTimed` or classes
 * with the same annotation and measures their execution time,
 * integrating with a metrics system through `MeterRegistryService`.
 *
 * This aspect supports handling reactive types such as `Mono` and `Flux` by applying metrics
 * on the reactive streams and providing additional context through tags.
 *
 * @property meterRegistryService The service managing interactions with the metrics registry for
 * recording and querying metrics.
 * @property tapsiMetricProperties Configuration properties that define the behavior of reactive
 * timing, including default metric properties and tag handling.
 */
@Aspect
@Component
class ReactiveTimedAspect(
  private val meterRegistryService: MeterRegistryService,
  private val tapsiMetricProperties: TapsiMetricProperties,
) : Ordered {
  val defaultMetricName = "reactive.method.timed"

  override fun getOrder(): Int = tapsiMetricProperties.reactiveTimed.order

  @Around(
    "@within(box.tapsi.libs.metrics.core.annotations.ReactiveTimed)" +
      "&& !@annotation(box.tapsi.libs.metrics.core.annotations.ReactiveTimed)",
  )
  fun reactiveTimedClass(joinPoint: ProceedingJoinPoint): Any? {
    val method = (joinPoint.signature as MethodSignature).method
    val targetMethod = AopUtils.getMostSpecificMethod(method, joinPoint.target.javaClass)
    val declaringClass = targetMethod.declaringClass
    val reactiveTimedAnnotation =
      AnnotationUtils.findAnnotation(declaringClass, ReactiveTimed::class.java) ?: return joinPoint.proceed()
    return perform(joinPoint, reactiveTimedAnnotation, targetMethod)
  }

  @Around("execution (@box.tapsi.libs.metrics.core.annotations.ReactiveTimed * *.*(..))")
  fun reactiveTimedMethod(joinPoint: ProceedingJoinPoint): Any? {
    val method = (joinPoint.signature as MethodSignature).method
    val targetMethod = AopUtils.getMostSpecificMethod(method, joinPoint.target.javaClass)
    val reactiveTimedAnnotation =
      AnnotationUtils.findAnnotation(targetMethod, ReactiveTimed::class.java) ?: return joinPoint.proceed()
    return perform(joinPoint, reactiveTimedAnnotation, targetMethod)
  }

  fun perform(joinPoint: ProceedingJoinPoint, reactiveTimedAnnotation: ReactiveTimed, method: Method): Any? {
    val result = joinPoint.proceed()
    val name = reactiveTimedAnnotation.name.ifEmpty { defaultMetricName }
    val extraTags = reactiveTimedAnnotation.extraTags
    val tags: MutableMap<String, String> = getTags(joinPoint, method, extraTags)
    if (result !is Publisher<*>) return result
    return applyReactiveMetrics(result, name, tags)
  }

  private fun getTags(
    joinPoint: ProceedingJoinPoint,
    method: Method,
    extraTags: Array<String>,
  ): MutableMap<String, String> {
    val tags: MutableMap<String, String> = mutableMapOf()

    if (tapsiMetricProperties.reactiveTimed.includeClassName) {
      tags["class"] = joinPoint.target.javaClass.name
    }
    if (tapsiMetricProperties.reactiveTimed.includeMethodName) {
      tags["method"] = method.name
    }

    tapsiMetricProperties.reactiveTimed.defaultTags.forEach { (key, value) ->
      tags[key] = value
    }

    for (i in 0 until (extraTags.size - 1) step 2) {
      tags[extraTags[i]] = extraTags[i + 1]
    }
    return tags
  }

  private fun applyReactiveMetrics(
    result: Publisher<*>,
    name: String,
    tags: Map<String, String>,
  ): Publisher<*> = when (result) {
    is Mono<*> -> {
      var unRecordedResult = result.name(name)
      for (tag in tags) {
        unRecordedResult = unRecordedResult.tag(tag.key, tag.value)
      }
      (unRecordedResult as Mono<Any>).`as`(meterRegistryService::tap)
    }

    is Flux<*> -> {
      var unRecordedResult = result
        .name(name)
      for (tag in tags) {
        unRecordedResult = unRecordedResult.tag(tag.key, tag.value)
      }
      (unRecordedResult as Flux<Any>).`as`(meterRegistryService::tap)
    }

    else -> result
  }
}
