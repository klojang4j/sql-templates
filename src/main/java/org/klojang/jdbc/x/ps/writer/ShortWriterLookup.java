package org.klojang.jdbc.x.ps.writer;

import org.klojang.convert.Bool;
import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.ps.ColumnWriter;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;

public final class ShortWriterLookup extends ColumnWriterLookup<Short> {

  public static final ColumnWriter<Short, Short> DEFAULT = new ColumnWriter<>(SET_SHORT);

  public ShortWriterLookup() {
    put(SMALLINT, DEFAULT);
    put(INTEGER, IntWriterLookup.DEFAULT);
    put(BIGINT, new ColumnWriter<>(SET_LONG));
    put(REAL, new ColumnWriter<>(SET_FLOAT));
    put(FLOAT, DoubleWriterLookup.DEFAULT);
    put(DOUBLE, DoubleWriterLookup.DEFAULT);
    addMultiple(new ColumnWriter<>(SET_BIG_DECIMAL), NUMERIC, DECIMAL);
    put(TINYINT, new ColumnWriter<Short, Byte>(SET_BYTE, NumberMethods::convert));
    addMultiple(new ColumnWriter<>(SET_BOOLEAN, Bool::from), BOOLEAN, BIT);
    put(VARCHAR, ColumnWriter.ANY_TO_STRING);
    put(CHAR, ColumnWriter.ANY_TO_STRING);
  }


}
