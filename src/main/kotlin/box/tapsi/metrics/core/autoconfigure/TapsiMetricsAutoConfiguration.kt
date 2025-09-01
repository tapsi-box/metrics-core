package box.tapsi.metrics.core.autoconfigure

import box.tapsi.metrics.core.TapsiMetricProperties
import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.observation.ObservationRegistry
import io.micrometer.observation.aop.ObservedAspect
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnClass(MeterRegistry::class)
@EnableConfigurationProperties(TapsiMetricProperties::class)
@AutoConfigureAfter(CompositeMeterRegistryAutoConfiguration::class)
@ComponentScan
class TapsiMetricsAutoConfiguration {

  @ConditionalOnMissingBean(TimedAspect::class)
  @ConditionalOnBean(MeterRegistry::class)
  @Bean
  fun timedAspect(meterRegistry: MeterRegistry): TimedAspect = TimedAspect(meterRegistry)

  @ConditionalOnMissingBean(ObservedAspect::class)
  @ConditionalOnBean(ObservationRegistry::class)
  @Bean
  fun observedAspect(observationRegistry: ObservationRegistry): ObservedAspect = ObservedAspect(observationRegistry)
}
