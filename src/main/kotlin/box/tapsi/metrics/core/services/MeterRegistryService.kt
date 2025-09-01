package box.tapsi.metrics.core.services

import box.tapsi.metrics.core.MeterName
import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.Tag
import kotlin.reflect.KClass
import reactor.core.publisher.Mono

/**
 * Service interface for interacting with MeterRegistry to manage application metrics.
 * Provides methods to record, query, and register various types of metrics like counters,
 * timers, gauges, and distribution summaries.
 */
interface MeterRegistryService {
  /**
   * Retrieves a meter associated with the given meter name and class.
   * The meter is identified based on its name and the "class" tag that matches the provided class name.
   *
   * @param meterName The name of the meter to be retrieved.
   * @param clazz The class used as a tag to identify the meter.
   * @return A Mono that emits the found Meter or completes empty if no matching meter is found.
   */
  fun getMeterOfClass(meterName: MeterName, clazz: KClass<*>): Mono<Meter>

  /**
   * Calculates the average execution time for a meter identified by the given meter name and class.
   * Combines the total time and count from the meter's statistical data to compute the average execution time.
   *
   * @param meterName The name of the meter to retrieve execution time statistics from.
   * @param clazz The class to be used as a tag to identify the meter.
   * @return A Mono emitting the computed average execution time, or an error if the meter cannot be found
   *         or the calculation fails.
   */
  fun getAverageExecutionTime(meterName: MeterName, clazz: KClass<*>): Mono<Double>

  /**
   * Increments the counter associated with the specified meter name and tags.
   *
   * @param meterName The name of the meter whose counter is to be incremented.
   * @param tags The list of tags associated with the meter to provide additional context for the metric.
   */
  fun incrementCounter(meterName: MeterName, tags: List<Tag>)

  /**
   * Records the elapsed time for a specific timer metric identified by the given name and tags.
   *
   * @param meterName The name of the timer metric to record the time for.
   * @param time The elapsed time in milliseconds that should be recorded.
   * @param tags A list of tags to associate with the timer metric, providing contextual metadata.
   */
  fun recordTimer(meterName: MeterName, time: Long, tags: List<Tag>)

  /**
   * Register a gauge with a function that returns the value to be measured.
   * The function will be called on each metric collection to get the value.
   *
   * @param meterName The name of the meter to register.
   * @param tags The tags to be added to the meter.
   * @param obj The object to be measured.
   * @param valueFunction The function that returns the value to be measured.
   */
  fun <T : Any> registerGauge(meterName: MeterName, tags: List<Tag>, obj: T, valueFunction: (T) -> Double)

  /**
   * Records a measurement value for a distribution summary metric identified by the given meter name and tags.
   *
   * @param meterName The name of the meter to record the distribution summary for.
   * @param tags A list of tags that provide contextual metadata for the metric.
   * @param value The value to record in the distribution summary.
   * @param baseUnit The base unit of measurement for the metric, or null if no base unit is specified.
   */
  fun distributionSummary(meterName: MeterName, tags: List<Tag>, value: Double, baseUnit: String?)
}
