package org.klojang.jdbc.x.rs;

import org.klojang.jdbc.x.rs.reader.*;

import java.util.HashMap;
import java.util.stream.IntStream;

/**
 * Maps SQL types (the static final int constants of java.sql.SQLType) to ResultSetReader
 * instances. Used when copying values from a ResultSet to a JavaBean. For JavaBeans the
 * value must fit the type of the target property, and hence the value produced by
 * ResultSet.getXXX() may have to be further processed by an Adapter that massages the
 * value from getXXX() such that can be assigned to the target property.
 */
public sealed class ResultSetReaderLookup<T> extends HashMap<Integer, ResultSetReader<?, ?>>
      permits
      BooleanReaders,
      ByteReaders,
      DoubleReaders,
      EnumReaders,
      FloatReaders,
      IntReaders,
      LocalDateReaders,
      LocalDateTimeReaders,
      LongReaders,
      ShortReaders,
      StringReaders {

  public ResultSetReaderLookup() {
    super();
  }

  public void add(int sqlType, ResultSetReader<?, T> extractor) {
    put(sqlType, extractor);
  }

  public void addMultiple(ResultSetReader<?, T> extractor, int... sqlTypes) {
    IntStream.of(sqlTypes).forEach(i -> put(i, extractor));
  }

}
