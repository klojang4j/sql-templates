package org.klojang.jdbc.x.ps.writer;

import org.klojang.convert.Bool;
import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.ps.PreparedStatementMethod;
import org.klojang.jdbc.x.ps.ColumnWriter;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;

public final class FloatWriterLookup extends ColumnWriterLookup<Float> {

  public static final ColumnWriter<Float, ?> DEFAULT = new ColumnWriter<>(PreparedStatementMethod.SET_FLOAT);

  public FloatWriterLookup() {
    put(REAL, DEFAULT);
    addMultiple(new ColumnWriter<>(SET_DOUBLE, NumberMethods::convert), FLOAT, DOUBLE);
    put(BIGINT, new ColumnWriter<Float, Long>(SET_LONG, NumberMethods::convert));
    put(INTEGER, new ColumnWriter<Float, Integer>(SET_INT, NumberMethods::convert));
    put(SMALLINT, new ColumnWriter<Float, Short>(SET_SHORT, NumberMethods::convert));
    put(TINYINT, new ColumnWriter<Float, Byte>(SET_BYTE, NumberMethods::convert));
    addMultiple(new ColumnWriter<>(SET_BOOLEAN, Bool::from), BOOLEAN, BIT);
    put(VARCHAR, ColumnWriter.ANY_TO_STRING);
    put(CHAR, ColumnWriter.ANY_TO_STRING);
  }


}
