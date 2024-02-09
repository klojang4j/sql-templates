package org.klojang.jdbc.x.ps.writer;

import org.klojang.convert.Bool;
import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.ps.ColumnWriter;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.ColumnWriter.ANY_TO_STRING;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;

public final class ShortWriterLookup extends ColumnWriterLookup<Short> {

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static final ColumnWriter DEFAULT = new ColumnWriter(SET_SHORT);

  @SuppressWarnings("unchecked")
  public ShortWriterLookup() {
    put(BOOLEAN, shortToBoolean());
    put(TINYINT, shortToByte());
    put(SMALLINT, DEFAULT);
    put(INTEGER, IntWriterLookup.DEFAULT);
    put(BIGINT, LongWriterLookup.DEFAULT);
    put(REAL, FloatWriterLookup.DEFAULT);
    addMultiple(DoubleWriterLookup.DEFAULT, DOUBLE, FLOAT);
    addMultiple(BigDecimalWriterLookup.DEFAULT, NUMERIC, DECIMAL);
    addMultiple(ANY_TO_STRING, VARCHAR, CHAR);
  }

  private static ColumnWriter<Object, Boolean> shortToBoolean() {
    return new ColumnWriter<>(SET_BOOLEAN, Bool::from);
  }

  private static ColumnWriter<Short, Byte> shortToByte() {
    return new ColumnWriter<>(SET_BYTE, NumberMethods::convert);
  }


}
