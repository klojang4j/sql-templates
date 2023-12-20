package org.klojang.jdbc.x.ps.writer;

import org.klojang.jdbc.x.ps.ColumnWriterLookup;
import org.klojang.jdbc.x.ps.PreparedStatementMethod;
import org.klojang.jdbc.x.ps.ColumnWriter;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;

public final class EnumWriterLookup extends ColumnWriterLookup<Enum<?>> {

  public static final ColumnWriter<Enum<?>, Integer> DEFAULT = new ColumnWriter<>(PreparedStatementMethod.SET_INT,
        Enum::ordinal);

  public static final ColumnWriter<Enum<?>, String> ENUM_TO_STRING =
        new ColumnWriter<>(PreparedStatementMethod.SET_STRING, Object::toString);

  public EnumWriterLookup() {
    put(INTEGER, DEFAULT);
    put(BIGINT,
          new ColumnWriter<Enum<?>, Long>(SET_LONG, e -> (long) e.ordinal()));
    put(SMALLINT,
          new ColumnWriter<Enum<?>, Short>(SET_SHORT, e -> (short) e.ordinal()));
    put(TINYINT,
          new ColumnWriter<Enum<?>, Byte>(SET_BYTE, e -> (byte) e.ordinal()));
    put(VARCHAR, ENUM_TO_STRING);
    put(CHAR, ENUM_TO_STRING);
  }

}
