package org.klojang.jdbc.x.ps.writer;

import org.klojang.jdbc.x.ps.ColumnWriter;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;

public final class EnumWriterLookup extends ColumnWriterLookup<Enum<?>> {

  @SuppressWarnings("rawtypes")
  public static final ColumnWriter DEFAULT = ordinalToInt();
  @SuppressWarnings("rawtypes")
  public static final ColumnWriter ENUM_TO_STRING = enumToString();

  public EnumWriterLookup() {
    put(TINYINT, ordinalToByte());
    put(SMALLINT, ordinalToShort());
    put(INTEGER, DEFAULT);
    put(BIGINT, ordinalToLong());
    addMultiple(enumToString(), VARCHAR, CHAR);
  }

  private static ColumnWriter<Enum<?>, Byte> ordinalToByte() {
    return new ColumnWriter<>(SET_BYTE, e -> (byte) e.ordinal());
  }

  private static ColumnWriter<Enum<?>, Short> ordinalToShort() {
    return new ColumnWriter<>(SET_SHORT, e -> (short) e.ordinal());
  }

  private static ColumnWriter<Enum<?>, Integer> ordinalToInt() {
    return new ColumnWriter<>(SET_INT, Enum::ordinal);
  }

  private static ColumnWriter<Enum<?>, Long> ordinalToLong() {
    return new ColumnWriter<>(SET_LONG, e -> (long) e.ordinal());
  }

  private static ColumnWriter<Enum<?>, String> enumToString() {
    return new ColumnWriter<>(SET_STRING, Object::toString);
  }


}
