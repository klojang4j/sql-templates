package org.klojang.jdbc.x.ps;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import org.klojang.collections.TypeMap;
import org.klojang.jdbc.x.ps.writer.*;

@SuppressWarnings("rawtypes")
final class DefaultWriters {

  static final DefaultWriters INSTANCE = new DefaultWriters();

  private Map<Class<?>, ColumnWriter> defaults;

  private DefaultWriters() {
    defaults = TypeMap.<ColumnWriter>fixedTypeMapBuilder()
        .autobox(true)
        .add(String.class, StringWriterLookup.DEFAULT)
        .add(Integer.class, IntWriterLookup.DEFAULT)
        .add(Boolean.class, BooleanWriterLookup.DEFAULT)
        .add(Double.class, DoubleWriterLookup.DEFAULT)
        .add(Long.class, LongWriterLookup.DEFAULT)
        .add(Float.class, FloatWriterLookup.DEFAULT)
        .add(Short.class, ShortWriterLookup.DEFAULT)
        .add(Byte.class, ByteWriterLookup.DEFAULT)
        .add(Enum.class, EnumWriterLookup.DEFAULT)
        .add(LocalDate.class, LocalDateWriterLookup.DEFAULT)
        .add(LocalDateTime.class, LocalDateTimeReceivers.DEFAULT)
        .freeze();
  }

  ColumnWriter getDefaultReceiver(Class<?> forType) {
    return defaults.get(forType);
  }

}
