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

  private Map<Class<?>, ColumnWriter> defaults;

  private DefaultWriters() {
    defaults = TypeMap.<ColumnWriter>fixedTypeMapBuilder()
          .autobox(true)
          .add(BigDecimal.class, BigDecimalWriterLookup.DEFAULT)
          .add(Boolean.class, BooleanWriterLookup.DEFAULT)
          .add(Byte.class, ByteWriterLookup.DEFAULT)
          .add(Double.class, DoubleWriterLookup.DEFAULT)
          .add(Enum.class, EnumWriterLookup.DEFAULT)
          .add(Float.class, FloatWriterLookup.DEFAULT)
          .add(Integer.class, IntWriterLookup.DEFAULT)
          .add(LocalDate.class, LocalDateWriterLookup.DEFAULT)
          .add(LocalDateTime.class, LocalDateTimeWriterLookup.DEFAULT)
          .add(Long.class, LongWriterLookup.DEFAULT)
          .add(Short.class, ShortWriterLookup.DEFAULT)
          .add(String.class, StringWriterLookup.DEFAULT)
          .freeze();
  }

  ColumnWriter getDefaultWriter(Class<?> forType) {
    ColumnWriter writer = defaults.get(forType);
    if (writer == null) {
      return ColumnWriter.ANY_TO_STRING;
    }
    return writer;
  }

}
