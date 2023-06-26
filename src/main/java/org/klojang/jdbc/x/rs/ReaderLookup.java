package org.klojang.jdbc.x.rs;

import java.util.HashMap;
import java.util.stream.IntStream;

/*
 * Maps SQL types (the static final int constants of java.sql.SQLType) to ResultSetReader
 * instances.
 */
sealed class ReaderLookup<T> extends HashMap<Integer, ResultSetReader<?, ?>> permits
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

  ReaderLookup() {
    super();
  }

  private ReaderLookup(int initialCapacity) {
    super(initialCapacity);
  }

  void add(int sqlType, ResultSetReader<?, T> extractor) {
    put(sqlType, extractor);
  }

  void addMultiple(ResultSetReader<?, T> extractor, int... sqlTypes) {
    IntStream.of(sqlTypes).forEach(i -> put(i, extractor));
  }

  ResultSetReader<?, T> getDefaultExtractor() {
    return null;
  }

  ReaderLookup<T> copy() {
    ReaderLookup<T> copy = new ReaderLookup<>((size() * 4) / 3 + 1);
    copy.putAll(this);
    return copy;
  }
}
