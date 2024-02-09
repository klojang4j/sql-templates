package org.klojang.jdbc.x.ps.writer;

import org.klojang.jdbc.x.ps.ColumnWriter;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.SET_BOOLEAN;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.SET_STRING;

public final class BooleanWriterLookup extends ColumnWriterLookup<Boolean> {

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static final ColumnWriter DEFAULT = new ColumnWriter(SET_BOOLEAN);

  public BooleanWriterLookup() {
    put(BOOLEAN, DEFAULT);
    addMultiple(new ColumnWriter<>(SET_STRING, this::asNumericString), VARCHAR, CHAR);
  }

  private String asNumericString(Boolean b) {
    return b == null || !b ? "0" : "1";
  }

}
