package box.tapsi.libs.metrics.core.services

import box.tapsi.libs.metrics.core.MeterName
import io.micrometer.core.instrument.DistributionSummary
import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Statistic
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.search.RequiredSearch
import io.micrometer.observation.ObservationRegistry
import org.slf4j.LoggerFactory
import reactor.core.observability.micrometer.Micrometer
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import kotlin.reflect.KClass

class MeterRegistryServiceImpl(
  private val registry: MeterRegistry,
  private val observationRegistry: ObservationRegistry,
) : MeterRegistryService {
  private val logger = LoggerFactory.getLogger(this::class.java)

  override fun incrementCounter(meterName: MeterName, tags: List<Tag>) {
    registry.counter(meterName.meterName, tags).increment()
  }

  override fun recordTimer(meterName: MeterName, time: Long, tags: List<Tag>) {
    registry.timer(meterName.meterName, tags).record(Duration.ofMillis(time))
  }

  override fun <T : Any> tap(flux: Flux<T>): Flux<T> = flux.contextCapture()
    .tap(Micrometer.observation(observationRegistry))

  override fun <T : Any> tap(mono: Mono<T>): Mono<T> = mono.contextCapture()
    .tap(Micrometer.observation(observationRegistry))

  override fun <T : Any> registerGauge(meterName: MeterName, tags: List<Tag>, obj: T, valueFunction: (T) -> Double) {
    registry.gauge(meterName.meterName, tags, obj, valueFunction)
  }

  override fun distributionSummary(meterName: MeterName, tags: List<Tag>, value: Double, baseUnit: String?) {
    DistributionSummary
      .builder(meterName.meterName)
      .tags(tags)
      .apply {
        baseUnit?.let(::baseUnit)
      }.register(registry)
      .record(value)
  }

  override fun getAverageExecutionTime(
    meterName: MeterName,
    clazz: KClass<*>,
  ): Mono<Double> = getMeterOfClass(meterName, clazz)
    .map { meter ->
      val totalTime = meter.measure().first { it.statistic == Statistic.TOTAL_TIME }.value
      val count = meter.measure().first { it.statistic == Statistic.COUNT }.value
      totalTime / count
    }.doOnNext {
      logger.info("Average execution time for ${clazz.java.name}: $it")
    }.doOnError {
      logger.error("Error while getting average execution time for ${clazz.java.name}: ${it.message}")
    }

  override fun getMeterOfClass(meterName: MeterName, clazz: KClass<*>): Mono<Meter> = Mono.fromCallable {
    return@fromCallable RequiredSearch.`in`(registry)
      .name { it.startsWith(meterName.meterName) }
      .tags("class", clazz.java.name)
      .meter()
  }.doOnNext {
    logger.info("Found meter for class ${clazz.java.name}: ${it.id}")
  }.doOnError {
    logger.error("Error while searching for meter for class ${clazz.java.name}: ${it.message}")
  }
}
