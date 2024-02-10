package org.klojang.jdbc.x.ps.writer;

import org.klojang.jdbc.x.ps.ValueBinder;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.SET_BOOLEAN;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.SET_STRING;

public final class BooleanBinderLookup extends ColumnWriterLookup<Boolean> {

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static final ValueBinder DEFAULT = new ValueBinder(SET_BOOLEAN);

  public BooleanBinderLookup() {
    put(BOOLEAN, DEFAULT);
    addMultiple(new ValueBinder<>(SET_STRING, this::asNumericString), VARCHAR, CHAR);
  }

  private String asNumericString(Boolean b) {
    return b == null || !b ? "0" : "1";
  }

}
