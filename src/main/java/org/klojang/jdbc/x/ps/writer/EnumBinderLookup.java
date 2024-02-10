package org.klojang.jdbc.x.ps.writer;

import org.klojang.jdbc.x.ps.ValueBinder;
import org.klojang.jdbc.x.ps.ValueBinderLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;
import static org.klojang.jdbc.x.ps.ValueBinder.ANY_TO_STRING;

public final class EnumBinderLookup extends ValueBinderLookup<Enum<?>> {

  @SuppressWarnings("rawtypes")
  public static final ValueBinder DEFAULT = ordinalToInt();

  public EnumBinderLookup() {
    put(TINYINT, ordinalToByte());
    put(SMALLINT, ordinalToShort());
    put(INTEGER, DEFAULT);
    put(BIGINT, ordinalToLong());
    addMultiple(ANY_TO_STRING, VARCHAR, CHAR);
  }

  private static ValueBinder<Enum<?>, Byte> ordinalToByte() {
    return new ValueBinder<>(SET_BYTE, e -> (byte) e.ordinal());
  }

  private static ValueBinder<Enum<?>, Short> ordinalToShort() {
    return new ValueBinder<>(SET_SHORT, e -> (short) e.ordinal());
  }

  private static ValueBinder<Enum<?>, Integer> ordinalToInt() {
    return new ValueBinder<>(SET_INT, Enum::ordinal);
  }

  private static ValueBinder<Enum<?>, Long> ordinalToLong() {
    return new ValueBinder<>(SET_LONG, e -> (long) e.ordinal());
  }

}
