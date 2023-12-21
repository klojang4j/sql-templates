package org.klojang.jdbc.x.ps;

import org.klojang.check.Check;
import org.klojang.collections.TypeMap;
import org.klojang.jdbc.x.ps.writer.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.klojang.check.CommonChecks.notNull;
import static org.klojang.jdbc.x.SQLTypeNames.getTypeName;
import static org.klojang.util.ClassMethods.className;

@SuppressWarnings({"rawtypes", "unchecked"})
class ColumnWriterFinder {

  private static ColumnWriterFinder INSTANCE;

  static ColumnWriterFinder getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ColumnWriterFinder();
    }
    return INSTANCE;
  }

  private final Map<Class<?>, Map<Integer, ColumnWriter>> all;

  private ColumnWriterFinder() {
    all = (Map<Class<?>, Map<Integer, ColumnWriter>>) createColumnWriters();
  }

  <T, U> ColumnWriter<T, U> getDefaultWriter(Class<T> fieldType) {
    return DefaultWriters.INSTANCE.getDefaultWriter(fieldType);
  }

  <T, U> ColumnWriter<T, U> findWriter(Class<T> fieldType, int sqlType) {
    // This implicitly checks that the specified int is one of the
    // static final int constants in the java.sql.Types class
    String sqlTypeName = getTypeName(sqlType);
    Map<Integer, ColumnWriter> writers = all.get(fieldType);
    Check.that(writers).is(notNull(), "Type not supported: ${0}", className(fieldType));
    ColumnWriter<T, U> writer = writers.get(sqlType);
    Check.that(writer).is(notNull(),
          "Cannot convert ${0} to ${1}",
          sqlTypeName,
          className(fieldType));
    return writer;
  }

  private static Map createColumnWriters() {
    return TypeMap.nativeTypeMapBuilder()
          .autobox(true)
          .add(String.class, immutable(new StringWriterLookup()))
          .add(Integer.class, immutable(new IntWriterLookup()))
          .add(Long.class, immutable(new LongWriterLookup()))
          .add(Double.class, immutable(new DoubleWriterLookup()))
          .add(Float.class, immutable(new FloatWriterLookup()))
          .add(Short.class, immutable(new ShortWriterLookup()))
          .add(Byte.class, immutable(new ByteWriterLookup()))
          .add(Boolean.class, immutable(new BooleanWriterLookup()))
          .add(LocalDate.class, immutable(new LocalDateWriterLookup()))
          .add(LocalDateTime.class, immutable(new LocalDateTimeWriterLookup()))
          .add(Enum.class, immutable(new EnumWriterLookup()))
          .freeze();
  }

  private static Map<Integer, ColumnWriter> immutable(ColumnWriterLookup src) {
    return Map.copyOf(src);
  }

}
