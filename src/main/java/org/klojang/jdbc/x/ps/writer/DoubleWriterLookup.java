package org.klojang.jdbc.x.ps.writer;

import org.klojang.convert.Bool;
import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.ps.ColumnWriter;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.ColumnWriter.ANY_TO_STRING;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;

public final class DoubleWriterLookup extends ColumnWriterLookup<Double> {

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static final ColumnWriter DEFAULT = new ColumnWriter(SET_DOUBLE);

  @SuppressWarnings("unchecked")
  public static <T> ColumnWriter<Double, T> defaultDoubleWriter() {
    return (ColumnWriter<Double, T>) DEFAULT;
  }

  @SuppressWarnings("unchecked")
  public DoubleWriterLookup() {
    put(BOOLEAN, doubleToBoolean());
    put(TINYINT, doubleToByte());
    put(SMALLINT, doubleToShort());
    put(INTEGER, doubleToInt());
    put(BIGINT, doubleToLong());
    put(REAL, doubleToFloat());
    addMultiple(defaultDoubleWriter(), DOUBLE, FLOAT);
    addMultiple(ANY_TO_STRING, VARCHAR, CHAR);
  }

  private static ColumnWriter<Object, Boolean> doubleToBoolean() {
    return new ColumnWriter<>(SET_BOOLEAN, Bool::from);
  }

  private static ColumnWriter<Double, Byte> doubleToByte() {
    return new ColumnWriter<>(SET_BYTE, NumberMethods::convert);
  }

  private static ColumnWriter<Double, Short> doubleToShort() {
    return new ColumnWriter<>(SET_SHORT, NumberMethods::convert);
  }

  private static ColumnWriter<Double, Integer> doubleToInt() {
    return new ColumnWriter<>(SET_INT, NumberMethods::convert);
  }

  private static ColumnWriter<Double, Long> doubleToLong() {
    return new ColumnWriter<>(SET_LONG, NumberMethods::convert);
  }

  private static ColumnWriter<Double, Float> doubleToFloat() {
    return new ColumnWriter<>(SET_FLOAT, NumberMethods::convert);
  }


}
