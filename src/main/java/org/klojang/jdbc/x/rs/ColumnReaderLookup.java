package org.klojang.jdbc.x.rs;

import org.klojang.jdbc.x.rs.reader.*;

import java.util.HashMap;
import java.util.stream.IntStream;

/**
 * Maps SQL types like VARCHAR to ColumnReader instances. Used when copying values from a
 * ResultSet to a JavaBean. The map keys are supposed to be integer constants from
 * {@link java.sql.Types java.sql.Types}.
 */
public sealed class ColumnReaderLookup<T> extends HashMap<Integer, ColumnReader<?, ?>>
      permits
      BooleanReaderLookup,
      ByteReaderLookup,
      DoubleReaderLookup,
      EnumReaderLookup,
      FloatReaderLookup,
      IntReaderLookup,
      LocalDateReaderLookup,
      LocalDateTimeReaderLookup,
      LongReaderLookup,
      ShortReaderLookup,
      StringReaderLookup,
      UUIDReaderLookup {

  public ColumnReaderLookup() {
    super();
  }

  public final void add(int sqlType, ColumnReader<?, T> reader) {
    put(sqlType, reader);
  }

  public final void addMultiple(ColumnReader<?, T> reader, int... sqlTypes) {
    IntStream.of(sqlTypes).forEach(i -> put(i, reader));
  }

}
