package org.klojang.jdbc.x.ps.writer;

import org.klojang.convert.Bool;
import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.ps.ColumnWriter;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.ColumnWriter.ANY_TO_STRING;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;

public final class FloatWriterLookup extends ColumnWriterLookup<Float> {

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static final ColumnWriter DEFAULT = new ColumnWriter(SET_FLOAT);

  @SuppressWarnings("unchecked")
  public FloatWriterLookup() {
    put(BOOLEAN, floatToBoolean());
    put(TINYINT, floatToByte());
    put(SMALLINT, floatToShort());
    put(INTEGER, floatToInt());
    put(BIGINT, floatToLong());
    addMultiple(DoubleWriterLookup.DEFAULT, DOUBLE, FLOAT, REAL);
    addMultiple(ANY_TO_STRING, VARCHAR, CHAR);
  }

  private static ColumnWriter<Object, Boolean> floatToBoolean() {
    return new ColumnWriter<>(SET_BOOLEAN, Bool::from);
  }

  private static ColumnWriter<Float, Byte> floatToByte() {
    return new ColumnWriter<>(SET_BYTE, NumberMethods::convert);
  }

  private static ColumnWriter<Float, Short> floatToShort() {
    return new ColumnWriter<>(SET_SHORT, NumberMethods::convert);
  }

  private static ColumnWriter<Float, Integer> floatToInt() {
    return new ColumnWriter<>(SET_INT, NumberMethods::convert);
  }

  private static ColumnWriter<Float, Long> floatToLong() {
    return new ColumnWriter<>(SET_LONG, NumberMethods::convert);
  }


}
