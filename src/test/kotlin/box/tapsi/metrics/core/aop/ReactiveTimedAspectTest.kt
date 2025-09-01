package box.tapsi.metrics.core.aop

import box.tapsi.metrics.core.TapsiMetricProperties
import box.tapsi.metrics.core.annotations.ReactiveTimed
import io.micrometer.observation.ObservationRegistry
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ReactiveTimedAspectTest {

  @Mock
  private lateinit var observationRegistry: ObservationRegistry

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private lateinit var tapsiMetricProperties: TapsiMetricProperties

  @Mock
  private lateinit var reactiveTimedProperties: TapsiMetricProperties.ReactiveTimed

  private lateinit var reactiveTimedAspect: ReactiveTimedAspect
  private lateinit var proxiedService: TestService

  @BeforeEach
  fun init() {
    MockitoAnnotations.openMocks(this)

    // Setup default properties
    Mockito.`when`(tapsiMetricProperties.reactiveTimed).thenReturn(reactiveTimedProperties)
    Mockito.`when`(reactiveTimedProperties.order).thenReturn(0)
    Mockito.`when`(reactiveTimedProperties.includeClassName).thenReturn(true)
    Mockito.`when`(reactiveTimedProperties.includeMethodName).thenReturn(true)
    Mockito.`when`(reactiveTimedProperties.defaultTags).thenReturn(emptyMap())

    // Create the aspect
    reactiveTimedAspect = ReactiveTimedAspect(observationRegistry, tapsiMetricProperties)

    // Create the target service and proxy with the aspect
    val testService = TestService()
    val factory = AspectJProxyFactory(testService)
    factory.addAspect(reactiveTimedAspect)
    proxiedService = factory.getProxy()
  }

  @Test
  fun `should have correct default metric name`() {
    assert(reactiveTimedAspect.defaultMetricName == "reactive.method.timed")
  }

  @Test
  fun `should handle reactive timed class annotation with Mono return`() {
    // when
    val actualResult = proxiedService.monoMethod()

    // verify
    StepVerifier.create(actualResult)
      .expectNext("test-result")
      .verifyComplete()
  }

  @Test
  fun `should handle reactive timed class annotation with Flux return`() {
    // when
    val actualResult = proxiedService.fluxMethod()

    // verify
    StepVerifier.create(actualResult)
      .expectNext("test1", "test2")
      .verifyComplete()
  }

  @Test
  fun `should handle reactive timed method annotation with Mono return`() {
    // when
    val actualResult = proxiedService.methodAnnotatedMono()

    // verify
    StepVerifier.create(actualResult)
      .expectNext("method-result")
      .verifyComplete()
  }

  @Test
  fun `should handle reactive timed method annotation with Flux return`() {
    // when
    val actualResult = proxiedService.methodAnnotatedFlux()

    // verify
    StepVerifier.create(actualResult)
      .expectNext("method1", "method2")
      .verifyComplete()
  }

  @Test
  fun `should handle non-reactive return type`() {
    // when
    val actualResult = proxiedService.nonReactiveMethod()

    // verify
    assert(actualResult == "non-reactive-result")
  }

  @Test
  fun `should handle method with custom metric name`() {
    // when
    val actualResult = proxiedService.customNamedMethod()

    // verify
    StepVerifier.create(actualResult)
      .expectNext("custom-name-result")
      .verifyComplete()
  }

  @Test
  fun `should handle method with extra tags`() {
    // when
    val actualResult = proxiedService.taggedMethod()

    // verify
    StepVerifier.create(actualResult)
      .expectNext("tagged-result")
      .verifyComplete()
  }

  @Test
  fun `should handle method with empty metric name`() {
    // when
    val actualResult = proxiedService.emptyNamedMethod()

    // verify
    StepVerifier.create(actualResult)
      .expectNext("empty-name-result")
      .verifyComplete()
  }

  @Test
  fun `should handle method with no extra tags`() {
    // when
    val actualResult = proxiedService.noTagsMethod()

    // verify
    StepVerifier.create(actualResult)
      .expectNext("no-tags-result")
      .verifyComplete()
  }

  @Test
  fun `should handle method without annotation`() {
    // when
    val actualResult = proxiedService.noAnnotationMethod()

    // verify
    assert(actualResult == "no-annotation-result")
  }

  @ReactiveTimed("test-service")
  open class TestService {

    fun monoMethod(): Mono<String> = Mono.just("test-result")

    fun fluxMethod(): Flux<String> = Flux.just("test1", "test2")

    @ReactiveTimed("method-metric")
    fun methodAnnotatedMono(): Mono<String> = Mono.just("method-result")

    @ReactiveTimed("method-flux-metric")
    fun methodAnnotatedFlux(): Flux<String> = Flux.just("method1", "method2")

    fun nonReactiveMethod(): String = "non-reactive-result"

    @ReactiveTimed("custom-metric-name")
    fun customNamedMethod(): Mono<String> = Mono.just("custom-name-result")

    @ReactiveTimed("tagged-metric", extraTags = ["tag1", "value1", "tag2", "value2"])
    fun taggedMethod(): Mono<String> = Mono.just("tagged-result")

    @ReactiveTimed("")
    fun emptyNamedMethod(): Mono<String> = Mono.just("empty-name-result")

    @ReactiveTimed("no-tags-metric", extraTags = [])
    fun noTagsMethod(): Mono<String> = Mono.just("no-tags-result")

    fun noAnnotationMethod(): String = "no-annotation-result"
  }
}
