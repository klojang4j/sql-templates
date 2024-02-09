package org.klojang.jdbc.x.ps.writer;

import org.klojang.jdbc.x.ps.ColumnWriter;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.SET_BOOLEAN;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.SET_STRING;

public final class BooleanWriterLookup extends ColumnWriterLookup<Boolean> {

  public static final ColumnWriter<Boolean, ?> DEFAULT = new ColumnWriter<>(SET_BOOLEAN);

  public BooleanWriterLookup() {
    put(BOOLEAN, DEFAULT);
    put(BIT, DEFAULT);
    addMultiple(new ColumnWriter<>(SET_STRING, this::asNumberString), VARCHAR, CHAR);
  }

  private String asNumberString(Boolean b) {
    return b == null || !b ? "0" : "1";
  }

}
