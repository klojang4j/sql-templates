package org.klojang.jdbc.x.ps;

import org.klojang.check.Check;
import org.klojang.collections.TypeMap;
import org.klojang.jdbc.x.ps.writer.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.klojang.check.CommonChecks.notNull;
import static org.klojang.jdbc.util.SQLTypeUtil.getTypeName;
import static org.klojang.util.ClassMethods.className;

@SuppressWarnings({"rawtypes", "unchecked"})
final class ValueBinderFactory {

  private static final ValueBinderFactory INSTANCE = new ValueBinderFactory();

  static ValueBinderFactory getInstance() { return INSTANCE; }

  private final Map<Class<?>, Map<Integer, ValueBinder>> allBinders;

  private ValueBinderFactory() {
    allBinders = (Map<Class<?>, Map<Integer, ValueBinder>>) createBinders();
  }

  <T, U> ValueBinder<T, U> getDefaultBinder(Class<T> fieldType) {
    return DefaultBinders.INSTANCE.getDefaultBinder(fieldType);
  }

  <T, U> ValueBinder<T, U> getBinder(Class<T> inputType, int targetSqlType) {
    // This implicitly checks that the specified int is one of the
    // static final int constants in the java.sql.Types class
    String sqlTypeName = getTypeName(targetSqlType);
    Map<Integer, ValueBinder> binders = allBinders.get(inputType);
    Check.that(binders).is(notNull(), "Type not supported: ${0}", className(inputType));
    ValueBinder<T, U> binder = binders.get(targetSqlType);
    Check.that(binder).is(notNull(),
          "Cannot convert ${0} to ${1}",
          sqlTypeName,
          className(inputType));
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
