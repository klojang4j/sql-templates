package org.klojang.jdbc.x.ps.writer;

import org.klojang.convert.Bool;
import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.ps.ValueBinder;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.ValueBinder.ANY_TO_STRING;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;

public final class DoubleBinderLookup extends ColumnWriterLookup<Double> {

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static final ValueBinder DEFAULT = new ValueBinder(SET_DOUBLE);

  @SuppressWarnings("unchecked")
  public static <T> ValueBinder<Double, T> defaultDoubleWriter() {
    return (ValueBinder<Double, T>) DEFAULT;
  }

  @SuppressWarnings("unchecked")
  public DoubleBinderLookup() {
    put(BOOLEAN, doubleToBoolean());
    put(TINYINT, doubleToByte());
    put(SMALLINT, doubleToShort());
    put(INTEGER, doubleToInt());
    put(BIGINT, doubleToLong());
    put(REAL, doubleToFloat());
    addMultiple(defaultDoubleWriter(), DOUBLE, FLOAT);
    addMultiple(ANY_TO_STRING, VARCHAR, CHAR);
  }

  private static ValueBinder<Object, Boolean> doubleToBoolean() {
    return new ValueBinder<>(SET_BOOLEAN, Bool::from);
  }

  private static ValueBinder<Double, Byte> doubleToByte() {
    return new ValueBinder<>(SET_BYTE, NumberMethods::convert);
  }

  private static ValueBinder<Double, Short> doubleToShort() {
    return new ValueBinder<>(SET_SHORT, NumberMethods::convert);
  }

  private static ValueBinder<Double, Integer> doubleToInt() {
    return new ValueBinder<>(SET_INT, NumberMethods::convert);
  }

  private static ValueBinder<Double, Long> doubleToLong() {
    return new ValueBinder<>(SET_LONG, NumberMethods::convert);
  }

  private static ValueBinder<Double, Float> doubleToFloat() {
    return new ValueBinder<>(SET_FLOAT, NumberMethods::convert);
  }


}
