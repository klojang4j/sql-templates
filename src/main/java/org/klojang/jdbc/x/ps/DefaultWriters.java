package org.klojang.jdbc.x.ps;

import org.klojang.collections.TypeMap;
import org.klojang.jdbc.x.ps.writer.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@SuppressWarnings("rawtypes")
final class DefaultWriters {

  static final DefaultWriters INSTANCE = new DefaultWriters();

  private Map<Class<?>, ValueBinder> defaults;

  private DefaultWriters() {
    defaults = TypeMap.<ValueBinder>fixedTypeMapBuilder()
          .autobox(true)
          .add(BigDecimal.class, BigDecimalBinderLookup.DEFAULT)
          .add(Boolean.class, BooleanBinderLookup.DEFAULT)
          .add(Byte.class, ByteBinderLookup.DEFAULT)
          .add(Double.class, DoubleBinderLookup.DEFAULT)
          .add(Enum.class, EnumBinderLookup.DEFAULT)
          .add(Float.class, FloatBinderLookup.DEFAULT)
          .add(Integer.class, IntBinderLookup.DEFAULT)
          .add(LocalDate.class, LocalDateBinderLookup.DEFAULT)
          .add(LocalDateTime.class, LocalDateTimeBinderLookup.DEFAULT)
          .add(Long.class, LongBinderLookup.DEFAULT)
          .add(Short.class, ShortBinderLookup.DEFAULT)
          .add(String.class, StringBinderLookup.DEFAULT)
          .freeze();
  }

  ValueBinder getDefaultWriter(Class<?> forType) {
    ValueBinder writer = defaults.get(forType);
    if (writer == null) {
      return ValueBinder.ANY_TO_STRING;
    }
    return writer;
  }

}
