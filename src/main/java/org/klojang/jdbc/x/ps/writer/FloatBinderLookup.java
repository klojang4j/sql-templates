package org.klojang.jdbc.x.ps.writer;

import org.klojang.convert.Bool;
import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.ps.ValueBinder;
import org.klojang.jdbc.x.ps.ValueBinderLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.ValueBinder.ANY_TO_STRING;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;

public final class FloatBinderLookup extends ValueBinderLookup<Float> {

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static final ValueBinder DEFAULT = new ValueBinder(SET_FLOAT);

  @SuppressWarnings("unchecked")
  public FloatBinderLookup() {
    put(BOOLEAN, floatToBoolean());
    put(TINYINT, floatToByte());
    put(SMALLINT, floatToShort());
    put(INTEGER, floatToInt());
    put(BIGINT, floatToLong());
    addMultiple(DoubleBinderLookup.DEFAULT, DOUBLE, FLOAT, REAL);
    addMultiple(ANY_TO_STRING, VARCHAR, CHAR);
  }

  private static ValueBinder<Object, Boolean> floatToBoolean() {
    return new ValueBinder<>(SET_BOOLEAN, Bool::from);
  }

  private static ValueBinder<Float, Byte> floatToByte() {
    return new ValueBinder<>(SET_BYTE, NumberMethods::convert);
  }

  private static ValueBinder<Float, Short> floatToShort() {
    return new ValueBinder<>(SET_SHORT, NumberMethods::convert);
  }

  private static ValueBinder<Float, Integer> floatToInt() {
    return new ValueBinder<>(SET_INT, NumberMethods::convert);
  }

  private static ValueBinder<Float, Long> floatToLong() {
    return new ValueBinder<>(SET_LONG, NumberMethods::convert);
  }


}
