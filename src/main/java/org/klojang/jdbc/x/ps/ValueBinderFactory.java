package org.klojang.jdbc.x.ps;

import org.klojang.collections.TypeMap;
import org.klojang.jdbc.x.Err;
import org.klojang.jdbc.x.ps.writer.*;
import org.klojang.util.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.klojang.jdbc.util.SQLTypeUtil.getTypeName;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.getObjectSetter;

@SuppressWarnings({"rawtypes", "unchecked"})
final class ValueBinderFactory {

  private static final Logger LOG = LoggerFactory.getLogger(ValueBinderFactory.class);

  private static final ValueBinderFactory INSTANCE = new ValueBinderFactory();

  static ValueBinderFactory getInstance() { return INSTANCE; }


  private final Map<Class<?>, Map<Integer, ValueBinder>> allBinders;
  private final Map<Tuple2<Class<?>, Integer>, ValueBinder> customBinders = new HashMap();

  private ValueBinderFactory() {
    allBinders = (Map<Class<?>, Map<Integer, ValueBinder>>) createBinders();
  }

  <T, U> ValueBinder<T, U> getDefaultBinder(Class<T> fieldType) {
    return DefaultBinders.INSTANCE.getDefaultBinder(fieldType);
  }

  <T, U> ValueBinder<T, U> getBinder(Class<T> inputType, int targetSqlType) {
    Map<Integer, ValueBinder> binders = allBinders.get(inputType);
    ValueBinder binder;
    if (binders == null) {
      Tuple2<Class<?>, Integer> key = Tuple2.of(inputType, targetSqlType);
      binder = customBinders.get(key);
      if (binder == null) {
        LOG.trace(Err.NO_PREDEFINED_BINDER, inputType.getName());
        binder = new ValueBinder<>(getObjectSetter(targetSqlType));
        customBinders.put(key, binder);
      }
    } else {
      binder = binders.get(targetSqlType);
      if (binder == null) {
        Tuple2<Class<?>, Integer> key = Tuple2.of(inputType, targetSqlType);
        binder = customBinders.get(key);
        if (binder == null) {
          LOG.trace(Err.NO_PREDEFINED_TYPE_MAPPING,
                inputType.getName(),
                getTypeName(targetSqlType));
          binder = new ValueBinder<>(getObjectSetter(targetSqlType));
          customBinders.put(key, binder);
        }
      }
    }
    return binder;
  }

  private static Map createBinders() {
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
          .add(Float.class, immutable(new FloatBinderLookup()))
          .add(Byte.class, immutable(new ByteBinderLookup()))
          .add(Short.class, immutable(new ShortBinderLookup()))
          .freeze();
  }

  private static Map<Integer, ValueBinder> immutable(ValueBinderLookup src) {
    return Map.copyOf(src);
  }

}
