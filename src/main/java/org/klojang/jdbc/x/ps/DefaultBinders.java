package org.klojang.jdbc.x.ps;

import org.klojang.collections.TypeMap;
import org.klojang.jdbc.x.Err;
import org.klojang.jdbc.x.ps.writer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.klojang.jdbc.x.ps.PreparedStatementMethod.SET_OBJECT;

@SuppressWarnings("rawtypes")
final class DefaultBinders {

  static final DefaultBinders INSTANCE = new DefaultBinders();

  private static final Logger LOG = LoggerFactory.getLogger(DefaultBinders.class);
  private static final ValueBinder LAST_RESORT = new ValueBinder<>(SET_OBJECT);

  private final Map<Class<?>, ValueBinder> defaults;

  private DefaultBinders() {
    defaults = TypeMap.<ValueBinder>nativeTypeMapBuilder()
          .autobox(true)
          .add(String.class, StringBinderLookup.DEFAULT)
          .add(Integer.class, IntBinderLookup.DEFAULT)
          .add(Double.class, DoubleBinderLookup.DEFAULT)
          .add(Long.class, LongBinderLookup.DEFAULT)
          .add(Boolean.class, BooleanBinderLookup.DEFAULT)
          .add(Enum.class, EnumBinderLookup.DEFAULT)
          .add(BigDecimal.class, BigDecimalBinderLookup.DEFAULT)
          .add(LocalDateTime.class, LocalDateTimeBinderLookup.DEFAULT)
          .add(LocalDate.class, LocalDateBinderLookup.DEFAULT)
          .add(Float.class, FloatBinderLookup.DEFAULT)
          .add(Short.class, ShortBinderLookup.DEFAULT)
          .add(Byte.class, ByteBinderLookup.DEFAULT)
          .freeze();
  }

  ValueBinder getDefaultBinder(Class forType) {
    ValueBinder binder = defaults.get(forType);
    if (binder == null) {
      LOG.trace(Err.NO_PREDEFINED_BINDER, forType.getName());
      return LAST_RESORT;
    }
    return binder;
  }

}
