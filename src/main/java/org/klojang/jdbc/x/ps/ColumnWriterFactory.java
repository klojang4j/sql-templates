package org.klojang.jdbc.x.ps;

import org.klojang.check.Check;
import org.klojang.collections.TypeMap;
import org.klojang.jdbc.x.ps.writer.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.klojang.check.CommonChecks.notNull;
import static org.klojang.jdbc.util.SQLTypeUtil.getTypeName;
import static org.klojang.util.ClassMethods.className;

@SuppressWarnings({"rawtypes", "unchecked"})
final class ColumnWriterFactory {

  private static final ColumnWriterFactory INSTANCE = new ColumnWriterFactory();

  static ColumnWriterFactory getInstance() { return INSTANCE; }

  private final Map<Class<?>, Map<Integer, ValueBinder>> all;

  private ColumnWriterFactory() {
    all = (Map<Class<?>, Map<Integer, ValueBinder>>) createColumnWriters();
  }

  <T, U> ValueBinder<T, U> getDefaultWriter(Class<T> fieldType) {
    return DefaultWriters.INSTANCE.getDefaultWriter(fieldType);
  }

  <T, U> ValueBinder<T, U> getWriter(Class<T> fieldType, int sqlType) {
    // This implicitly checks that the specified int is one of the
    // static final int constants in the java.sql.Types class
    String sqlTypeName = getTypeName(sqlType);
    Map<Integer, ValueBinder> writers = all.get(fieldType);
    Check.that(writers).is(notNull(), "Type not supported: ${0}", className(fieldType));
    ValueBinder<T, U> writer = writers.get(sqlType);
    Check.that(writer).is(notNull(),
          "Cannot convert ${0} to ${1}",
          sqlTypeName,
          className(fieldType));
    return writer;
  }

  private static Map createColumnWriters() {
    return TypeMap.nativeTypeMapBuilder()
          .autobox(true)
          .add(String.class, immutable(new StringBinderLookup()))
          .add(Integer.class, immutable(new IntBinderLookup()))
          .add(Long.class, immutable(new LongBinderLookup()))
          .add(Double.class, immutable(new DoubleBinderLookup()))
          .add(Float.class, immutable(new FloatBinderLookup()))
          .add(Short.class, immutable(new ShortBinderLookup()))
          .add(Byte.class, immutable(new ByteBinderLookup()))
          .add(Boolean.class, immutable(new BooleanBinderLookup()))
          .add(LocalDate.class, immutable(new LocalDateBinderLookup()))
          .add(LocalDateTime.class, immutable(new LocalDateTimeBinderLookup()))
          .add(Enum.class, immutable(new EnumBinderLookup()))
          .freeze();
  }

  private static Map<Integer, ValueBinder> immutable(ColumnWriterLookup src) {
    return Map.copyOf(src);
  }

}
