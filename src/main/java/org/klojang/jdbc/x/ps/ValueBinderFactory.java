package org.klojang.jdbc.x.ps;

import org.klojang.collections.TypeMap;
import org.klojang.jdbc.util.SQLTypeUtil;
import org.klojang.jdbc.x.ps.writer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.klojang.jdbc.x.Msg.NO_PREDEFINED_BINDER;
import static org.klojang.jdbc.x.Msg.NO_PREDEFINED_TYPE_MAPPING;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.getObjectSetter;
import static org.klojang.util.ClassMethods.className;
import static org.klojang.util.ClassMethods.simpleClassName;

@SuppressWarnings({"rawtypes", "unchecked"})
final class ValueBinderFactory {

  private static final Logger LOG = LoggerFactory.getLogger(ValueBinderFactory.class);
  private static final ValueBinderFactory INSTANCE = new ValueBinderFactory();

  static ValueBinderFactory getInstance() { return INSTANCE; }

  private record Key(Class inputType, int targetSqlType) { }

  private final Map<Class, Map<Integer, ValueBinder>> predefined;
  private final Map<Key, ValueBinder> custom = new HashMap();

  private ValueBinderFactory() {
    predefined = (Map<Class, Map<Integer, ValueBinder>>) getPredefinedBinders();
  }

  <T, U> ValueBinder<T, U> getDefaultBinder(Class<T> fieldType) {
    return DefaultBinders.INSTANCE.getDefaultBinder(fieldType);
  }

  <T, U> ValueBinder<T, U> getBinder(Class<T> inputType, int targetSqlType) {
    Map<Integer, ValueBinder> binders = predefined.get(inputType);
    ValueBinder binder;
    if (binders == null) {
      Key key = new Key(inputType, targetSqlType);
      binder = custom.get(key);
      if (binder == null) {
        if (LOG.isTraceEnabled()) {
          LOG.trace(NO_PREDEFINED_BINDER,
                className(inputType),
                simpleClassName(inputType));
        }
        binder = StringBinderLookup.DEFAULT;
        custom.put(key, binder);
      }
    } else {
      binder = binders.get(targetSqlType);
      if (binder == null) {
        Key key = new Key(inputType, targetSqlType);
        binder = custom.get(key);
        if (binder == null) {
          if (LOG.isTraceEnabled()) {
            String javaTypeName = className(inputType);
            String sqlTypeName = SQLTypeUtil.getTypeName(targetSqlType);
            LOG.trace(NO_PREDEFINED_TYPE_MAPPING, javaTypeName, sqlTypeName);
          }
          binder = new ValueBinder<>(getObjectSetter(targetSqlType));
          custom.put(key, binder);
        }
      }
    }
    return binder;
  }

  private static Map getPredefinedBinders() {
    return TypeMap.nativeTypeMapBuilder()
          .autobox(true)
          .add(String.class, immutable(new StringBinderLookup()))
          .add(Integer.class, immutable(new IntBinderLookup()))
          .add(Double.class, immutable(new DoubleBinderLookup()))
          .add(Long.class, immutable(new LongBinderLookup()))
          .add(Boolean.class, immutable(new BooleanBinderLookup()))
          .add(Enum.class, immutable(new EnumBinderLookup()))
          .add(BigDecimal.class, immutable(new BigDecimalBinderLookup()))
          .add(LocalDate.class, immutable(new LocalDateBinderLookup()))
          .add(LocalDateTime.class, immutable(new LocalDateTimeBinderLookup()))
          .add(byte[].class, immutable(new ByteArrayBinderLookup()))
          .add(Float.class, immutable(new FloatBinderLookup()))
          .add(Byte.class, immutable(new ByteBinderLookup()))
          .add(Short.class, immutable(new ShortBinderLookup()))
          .freeze();
  }

  private static Map<Integer, ValueBinder> immutable(ValueBinderLookup src) {
    return Map.copyOf(src);
  }

}
