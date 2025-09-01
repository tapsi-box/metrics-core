package box.tapsi.metrics.core.services

import box.tapsi.metrics.core.MeterName
import io.micrometer.core.instrument.DistributionSummary
import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Statistic
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.search.RequiredSearch
import java.time.Duration
import kotlin.reflect.KClass
import org.slf4j.Logger
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class MeterRegistryServiceImpl(
  private val registry: MeterRegistry,
  private val logger: Logger,
) : MeterRegistryService {
  override fun incrementCounter(meterName: MeterName, tags: List<Tag>) {
    registry.counter(meterName.meterName, tags).increment()
  }

  override fun recordTimer(meterName: MeterName, time: Long, tags: List<Tag>) {
    registry.timer(meterName.meterName, tags).record(Duration.ofMillis(time))
  }

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

  override fun getAverageExecutionTime(meterName: MeterName, clazz: KClass<*>): Mono<Double> =
    getMeterOfClass(meterName, clazz)
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
