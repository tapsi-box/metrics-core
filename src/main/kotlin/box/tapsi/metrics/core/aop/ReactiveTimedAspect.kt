package box.tapsi.metrics.core.aop

import box.tapsi.metrics.core.TapsiMetricProperties
import box.tapsi.metrics.core.annotations.ReactiveTimed
import io.micrometer.observation.ObservationRegistry
import java.lang.reflect.Method
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.aop.support.AopUtils
import org.springframework.core.Ordered
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.stereotype.Component
import reactor.core.CorePublisher
import reactor.core.observability.micrometer.Micrometer
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Aspect
@Component
class ReactiveTimedAspect(
  private val observationRegistry: ObservationRegistry,
  private val tapsiMetricProperties: TapsiMetricProperties,
) : Ordered {
  val defaultMetricName = "reactive.method.timed"

  override fun getOrder(): Int = tapsiMetricProperties.reactiveTimed.order

  @Around(
    "@within(box.tapsi.metrics.core.annotations.ReactiveTimed)" +
      "&& !@annotation(box.tapsi.metrics.core.annotations.ReactiveTimed)",
  )
  fun reactiveTimedClass(joinPoint: ProceedingJoinPoint): Any? {
    val method = (joinPoint.signature as MethodSignature).method
    val targetMethod = AopUtils.getMostSpecificMethod(method, joinPoint.target.javaClass)
    val declaringClass = targetMethod.declaringClass
    val reactiveTimedAnnotation =
      AnnotationUtils.findAnnotation(declaringClass, ReactiveTimed::class.java) ?: return joinPoint.proceed()
    return perform(joinPoint, reactiveTimedAnnotation, targetMethod)
  }

  @Around("execution (@box.tapsi.metrics.core.annotations.ReactiveTimed * *.*(..))")
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
    if (result !is CorePublisher<*>) return result
    return applyReactiveMetrics(result, name, tags)
  }

  private fun getTags(
    joinPoint: ProceedingJoinPoint,
    method: Method,
    extraTags: Array<String>,
  ): MutableMap<String, String> {
    val tags: MutableMap<String, String> = mutableMapOf()

    if (tapsiMetricProperties.reactiveTimed.includeClassName)
      tags["class"] = joinPoint.target.javaClass.name
    if (tapsiMetricProperties.reactiveTimed.includeMethodName)
      tags["method"] = method.name

    tapsiMetricProperties.reactiveTimed.defaultTags.forEach { (key, value) ->
      tags[key] = value
    }

    for (i in 0 until (extraTags.size - 1) step 2) {
      tags[extraTags[i]] = extraTags[i + 1]
    }
    return tags
  }

  private fun applyReactiveMetrics(
    result: CorePublisher<*>,
    name: String,
    tags: Map<String, String>,
  ): CorePublisher<*> = when (result) {
    is Mono<*> -> {
      var unRecordedResult = result.name(name)
      for (tag in tags) {
        unRecordedResult = unRecordedResult.tag(tag.key, tag.value)
      }
      (unRecordedResult as Mono<Any>).contextCapture().tap(Micrometer.observation(observationRegistry))
    }

    is Flux<*> -> {
      var unRecordedResult = result
        .name(name)
      for (tag in tags) {
        unRecordedResult = unRecordedResult.tag(tag.key, tag.value)
      }
      (unRecordedResult as Flux<Any>).contextCapture().tap(Micrometer.observation(observationRegistry))
    }

    else -> result
  }
}
