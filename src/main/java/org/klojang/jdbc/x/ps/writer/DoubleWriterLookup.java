package org.klojang.jdbc.x.ps.writer;

import org.klojang.convert.Bool;
import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.ps.ColumnWriter;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;

public final class DoubleWriterLookup extends ColumnWriterLookup<Double> {

  public static final ColumnWriter<Double, ?> DEFAULT = new ColumnWriter<>(SET_DOUBLE);

  public DoubleWriterLookup() {
    put(FLOAT, DEFAULT);
    put(DOUBLE, DEFAULT);
    put(BIGINT, new ColumnWriter<Double, Long>(SET_LONG, NumberMethods::convert));
    put(REAL, new ColumnWriter<Double, Float>(SET_FLOAT, NumberMethods::convert));
    put(INTEGER, new ColumnWriter<Double, Integer>(SET_INT, NumberMethods::convert));
    put(SMALLINT, new ColumnWriter<Double, Short>(SET_SHORT, NumberMethods::convert));
    put(TINYINT, new ColumnWriter<Double, Byte>(SET_BYTE, NumberMethods::convert));
    addMultiple(new ColumnWriter<>(SET_BOOLEAN, Bool::from), BOOLEAN, BIT);
    put(VARCHAR, ColumnWriter.ANY_TO_STRING);
    put(CHAR, ColumnWriter.ANY_TO_STRING);
  }


}
