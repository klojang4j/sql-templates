package org.klojang.jdbc.x.ps.writer;

import org.klojang.convert.Bool;
import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.ps.PreparedStatementMethod;
import org.klojang.jdbc.x.ps.ColumnWriter;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import static java.sql.Types.*;

public final class DoubleWriterLookup extends ColumnWriterLookup<Double> {

  public static final ColumnWriter<Double, ?> DEFAULT = new ColumnWriter<>(PreparedStatementMethod.SET_DOUBLE);

  public DoubleWriterLookup() {
    put(FLOAT, DEFAULT);
    put(DOUBLE, DEFAULT);
    put(BIGINT,
          new ColumnWriter<Double, Long>(PreparedStatementMethod.SET_LONG,
                NumberMethods::convert));
    put(REAL,
          new ColumnWriter<Double, Float>(PreparedStatementMethod.SET_FLOAT,
                NumberMethods::convert));
    put(INTEGER,
          new ColumnWriter<Double, Integer>(PreparedStatementMethod.SET_INT,
                NumberMethods::convert));
    put(SMALLINT,
          new ColumnWriter<Double, Short>(PreparedStatementMethod.SET_SHORT,
                NumberMethods::convert));
    put(TINYINT,
          new ColumnWriter<Double, Byte>(PreparedStatementMethod.SET_BYTE,
                NumberMethods::convert));
    addMultiple(new ColumnWriter<>(PreparedStatementMethod.SET_BOOLEAN, Bool::from),
          BOOLEAN,
          BIT);
    put(VARCHAR, ColumnWriter.ANY_TO_STRING);
    put(CHAR, ColumnWriter.ANY_TO_STRING);
  }


}
