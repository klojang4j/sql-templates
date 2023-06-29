package org.klojang.jdbc.x.ps;

import java.util.HashMap;
import java.util.stream.IntStream;

public abstract class ColumnWriterLookup<T> extends HashMap<Integer, ColumnWriter<?, ?>> {

  public ColumnWriterLookup() {}

  public void putMultiple(ColumnWriter<T, ?> receiver, int... sqlTypes) {
    IntStream.of(sqlTypes).forEach(i -> put(i, receiver));
  }

}
