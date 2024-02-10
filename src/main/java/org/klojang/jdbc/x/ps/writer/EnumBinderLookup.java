package org.klojang.jdbc.x.ps.writer;

import org.klojang.jdbc.x.ps.ValueBinder;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;

public final class EnumBinderLookup extends ColumnWriterLookup<Enum<?>> {

  @SuppressWarnings("rawtypes")
  public static final ValueBinder DEFAULT = ordinalToInt();
  @SuppressWarnings("rawtypes")
  public static final ValueBinder ENUM_TO_STRING = enumToString();

  public EnumBinderLookup() {
    put(TINYINT, ordinalToByte());
    put(SMALLINT, ordinalToShort());
    put(INTEGER, DEFAULT);
    put(BIGINT, ordinalToLong());
    addMultiple(enumToString(), VARCHAR, CHAR);
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

  private static ValueBinder<Enum<?>, String> enumToString() {
    return new ValueBinder<>(SET_STRING, Object::toString);
  }


}
