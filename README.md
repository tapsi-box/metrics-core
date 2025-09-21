# Tapsi Metrics Core

A reactive observability library for Spring Boot applications that provides comprehensive metrics collection and
monitoring capabilities.

## Features

- **Reactive Metrics**: Built-in support for Mono and Flux observability
- **Custom Annotations**: `@ReactiveTimed` annotation for method-level metrics
- **Flexible Configuration**: Configurable tags, class names, and method names
- **Spring Boot Auto-configuration**: Zero-config setup with Spring Boot 3.x

## Quick Start

### 1. Add Dependency

```xml

<dependency>
    <groupId>box.tapsi.libs</groupId>
    <artifactId>metrics-core</artifactId>
    <version>1.0.4</version>
</dependency>
```

### 2. Enable Metrics

The library automatically configures itself when you have the following dependencies in your project:

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
<groupId>io.micrometer</groupId>
<artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### 3. Use the Service

```kotlin
@Service
class MyService(
  private val meterRegistryService: MeterRegistryService
) {

  @ReactiveTimed(name = "my.custom.metric")
  fun myMethod(): Mono<String> {
    // Your reactive logic here
    return Mono.just("Hello World")
  }

  fun recordCustomMetric() {
    meterRegistryService.incrementCounter(
      object : MeterName {
        override val meterName = "custom.counter"
      },
      listOf(Tag.of("component", "my-service"))
    )
  }
}
```

## Configuration

### Properties

```yaml
box:
  libs:
    metrics:
      reactive-timed:
        order: 2147483647  # Lowest precedence
        include-class-name: true
        include-method-name: true
        default-tags:
          environment: production
          service: my-app
```

### Custom Meter Names

```kotlin
enum class MyMetrics : MeterName {
  REQUEST_COUNT,
  RESPONSE_TIME,
  ERROR_COUNT;

  override val meterName: String = name.lowercase().replace("_", ".")
}
```

## Troubleshooting

### Common Issues

#### 1. Bean Not Found Error

If you get this error:

```
Parameter 1 of constructor in ... required a bean of type 'box.tapsi.libs.metrics.core.services.MeterRegistryService' that could not be found.
```

**Solutions:**

1. **Ensure MeterRegistry is available**: Make sure you have a `MeterRegistry` bean in your application:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-actuator</artifactId>
   </dependency>
   ```

2. **Check Spring Boot version**: This library requires Spring Boot 3.x

3. **Enable auto-configuration**: Ensure auto-configuration is not disabled:
   ```kotlin
   @SpringBootApplication(exclude = [])
   ```

4. **Check package scanning**: Ensure your main application class can scan the `box.tapsi.libs.metrics.core` package

#### 2. Metrics Not Appearing

1. **Check Micrometer configuration**: Ensure you have a metrics registry configured
2. **Verify annotation usage**: Make sure `@ReactiveTimed` is applied to reactive methods
3. **Check logging**: Enable debug logging for `box.tapsi.libs.metrics.core` package

### Debug Mode

Enable debug logging to troubleshoot configuration issues:

```yaml
logging:
  level:
    box.tapsi.libs.metrics.core: DEBUG
```

## API Reference

### MeterRegistryService

- `getMeterOfClass(meterName: MeterName, clazz: KClass<*>): Mono<Meter>`
- `getAverageExecutionTime(meterName: MeterName, clazz: KClass<*>): Mono<Double>`
- `incrementCounter(meterName: MeterName, tags: List<Tag>)`
- `recordTimer(meterName: MeterName, time: Long, tags: List<Tag>)`
- `registerGauge(meterName: MeterName, tags: List<Tag>, obj: T, valueFunction: (T) -> Double)`
- `distributionSummary(meterName: MeterName, tags: List<Tag>, value: Double, baseUnit: String?)`
- `tap(mono: Mono<T>): Mono<T>`
- `tap(flux: Flux<T>): Flux<T>`

### ReactiveTimed Annotation

- `name`: Custom metric name (optional)
- `extraTags`: Additional key-value tags as array

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

MIT License - see LICENSE file for details.
