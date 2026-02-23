package box.tapsi.libs.metrics.core.accessors

import io.micrometer.context.ThreadLocalAccessor
import org.slf4j.MDC

class MdcKeyAccessor(
  private val key: String,
) : ThreadLocalAccessor<String> {
  override fun key(): Any = key

  override fun getValue(): String? = MDC.get(key)

  override fun setValue(value: String) {
    MDC.put(key, value)
  }

  override fun setValue() {
    MDC.remove(key)
  }
}
