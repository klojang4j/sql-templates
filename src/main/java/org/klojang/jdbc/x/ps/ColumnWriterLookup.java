package org.klojang.jdbc.x.ps;

import org.klojang.jdbc.x.ps.ColumnWriter;
import org.klojang.jdbc.x.ps.writer.*;

import java.util.HashMap;
import java.util.stream.IntStream;

public abstract sealed class ColumnWriterLookup<T>
      extends HashMap<Integer, ColumnWriter<?, ?>> permits BooleanWriterLookup,
      ByteWriterLookup,
      DoubleWriterLookup,
      EnumWriterLookup,
      FloatWriterLookup,
      IntWriterLookup,
      LocalDateTimeWriterLookup,
      LocalDateWriterLookup,
      LongWriterLookup,
      ShortWriterLookup,
      StringWriterLookup {

  public ColumnWriterLookup() { }

  public void addMultiple(ColumnWriter<T, ?> writer, int... sqlTypes) {
    IntStream.of(sqlTypes).forEach(i -> put(i, writer));
  }

}
