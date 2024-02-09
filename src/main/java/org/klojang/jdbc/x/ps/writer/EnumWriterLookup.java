package org.klojang.jdbc.x.ps.writer;

import org.klojang.jdbc.x.ps.ColumnWriter;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;

public final class EnumWriterLookup extends ColumnWriterLookup<Enum<?>> {

  public static final ColumnWriter<Enum<?>, Integer> DEFAULT = ordinalToInt();
  public static final ColumnWriter<Enum<?>, String> ENUM_TO_STRING = enumToString();

  public EnumWriterLookup() {
    put(INTEGER, DEFAULT);
    put(BIGINT, ordinalToLong());
    put(SMALLINT, ordinalToShort());
    put(TINYINT, ordinalToByte());
    addMultiple(enumToString(), VARCHAR, CHAR);
  }

  public static ColumnWriter<Enum<?>, Byte> ordinalToByte() {
    return new ColumnWriter<>(SET_BYTE, e -> (byte) e.ordinal());
  }

  public static ColumnWriter<Enum<?>, Short> ordinalToShort() {
    return new ColumnWriter<>(SET_SHORT, e -> (short) e.ordinal());
  }

  private static ColumnWriter<Enum<?>, Integer> ordinalToInt() {
    return new ColumnWriter<>(SET_INT, Enum::ordinal);
  }

  public static ColumnWriter<Enum<?>, Long> ordinalToLong() {
    return new ColumnWriter<>(SET_LONG, e -> (long) e.ordinal());
  }

  private static ColumnWriter<Enum<?>, String> enumToString() {
    return new ColumnWriter<>(SET_STRING, Object::toString);
  }


}
