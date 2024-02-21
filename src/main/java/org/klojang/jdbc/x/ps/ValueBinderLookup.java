package org.klojang.jdbc.x.ps;

import org.klojang.jdbc.x.ps.writer.*;

import java.util.HashMap;
import java.util.stream.IntStream;

public abstract sealed class ValueBinderLookup<T>
      extends HashMap<Integer, ValueBinder<?, ?>>
      permits BigDecimalBinderLookup,
      BooleanBinderLookup,
      ByteArrayBinderLookup,
      ByteBinderLookup,
      DoubleBinderLookup,
      EnumBinderLookup,
      FloatBinderLookup,
      IntBinderLookup,
      LocalDateTimeBinderLookup,
      LocalDateBinderLookup,
      LongBinderLookup,
      ShortBinderLookup,
      StringBinderLookup {

  public ValueBinderLookup() { }

  public <U> void addMultiple(ValueBinder<T, U> writer, int... sqlTypes) {
    IntStream.of(sqlTypes).forEach(i -> put(i, writer));
  }

}
