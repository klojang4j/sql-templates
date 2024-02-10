package org.klojang.jdbc.x.ps.writer;

import org.klojang.convert.Bool;
import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.ps.ValueBinder;
import org.klojang.jdbc.x.ps.ValueBinderLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.ValueBinder.ANY_TO_STRING;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;

public final class LongBinderLookup extends ValueBinderLookup<Long> {

  public static final ValueBinder<Long, Long> DEFAULT = new ValueBinder<>(SET_LONG);

  public LongBinderLookup() {
    put(BOOLEAN, longToBoolean());
    put(TINYINT, longToByte());
    put(SMALLINT, longToShort());
    put(INTEGER, longToInt());
    put(BIGINT, DEFAULT);
    addMultiple(ANY_TO_STRING, VARCHAR, CHAR);
  }

  private static ValueBinder<Long, Boolean> longToBoolean() {
    return new ValueBinder<>(SET_BOOLEAN, Bool::from);
  }

  private static ValueBinder<Long, Byte> longToByte() {
    return new ValueBinder<>(SET_BYTE, NumberMethods::convert);
  }

  private static ValueBinder<Long, Short> longToShort() {
    return new ValueBinder<>(SET_SHORT, NumberMethods::convert);
  }

  private static ValueBinder<Long, Integer> longToInt() {
    return new ValueBinder<>(SET_INT, NumberMethods::convert);
  }

}
