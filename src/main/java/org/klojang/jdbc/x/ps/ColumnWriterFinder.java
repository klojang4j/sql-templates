package org.klojang.jdbc.x.ps;

import org.klojang.check.Check;
import org.klojang.collections.TypeMap;
import org.klojang.jdbc.x.ps.writer.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.klojang.check.CommonChecks.notNull;
import static org.klojang.jdbc.x.SQLTypeNames.getTypeName;

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
    Map tmp = createReceivers(); // to hell with generics
    all = (Map<Class<?>, Map<Integer, ColumnWriter>>) tmp;
  }

  <T, U> ColumnWriter<T, U> getDefaultReceiver(Class<T> fieldType) {
    ColumnWriter receiver = DefaultWriters.INSTANCE.getDefaultReceiver(fieldType);
    return Check.that(receiver).is(notNull(), "Type not supported: {type}").ok();
  }

  <T, U> ColumnWriter<T, U> findReceiver(Class<T> fieldType, int sqlType) {
    // This implicitly checks that the specified int is one of the
    // static final int constants in the java.sql.Types class
    String sqlTypeName = getTypeName(sqlType);
    Map<Integer, ColumnWriter> receivers = all.get(fieldType);
    Check.that(receivers).is(notNull(), "Type not supported: {type}");
    ColumnWriter<T, U> receiver = receivers.get(sqlType);
    Check.that(receiver).is(notNull(), "Cannot convert {0} to {type}", sqlTypeName);
    return receiver;
  }

  private static Map createReceivers() {
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
        .add(LocalDateTime.class, immutable(new LocalDateTimeReceivers()))
        .add(Enum.class, immutable(new EnumWriterLookup()))
        .freeze();
  }

  private static Map<Integer, ColumnWriter> immutable(ColumnWriterLookup src) {
    return Map.copyOf(src);
  }

}
