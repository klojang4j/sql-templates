package org.klojang.jdbc.x.ps.writer;

import org.klojang.convert.Bool;
import org.klojang.jdbc.x.ps.ValueBinder;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.ValueBinder.ANY_TO_STRING;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.SET_BOOLEAN;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.SET_BYTE;

public final class ByteBinderLookup extends ColumnWriterLookup<Byte> {

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static final ValueBinder DEFAULT = new ValueBinder(SET_BYTE);

  @SuppressWarnings("unchecked")
  public ByteBinderLookup() {
    put(TINYINT, DEFAULT);
    put(BOOLEAN, new ValueBinder<Byte, Boolean>(SET_BOOLEAN, Bool::from));
    put(INTEGER, IntBinderLookup.DEFAULT);
    put(SMALLINT, ShortBinderLookup.DEFAULT);
    put(BIGINT, LongBinderLookup.DEFAULT);
    put(REAL, FloatBinderLookup.DEFAULT);
    addMultiple(DoubleBinderLookup.DEFAULT, DOUBLE, FLOAT);
    addMultiple(BigDecimalBinderLookup.DEFAULT, NUMERIC, DECIMAL);
    addMultiple(ANY_TO_STRING, VARCHAR, CHAR);
  }


}
