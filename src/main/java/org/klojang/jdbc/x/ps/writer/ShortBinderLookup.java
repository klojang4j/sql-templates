package org.klojang.jdbc.x.ps.writer;

import org.klojang.convert.Bool;
import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.ps.ValueBinder;
import org.klojang.jdbc.x.ps.ValueBinderLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.ValueBinder.ANY_TO_STRING;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;

public final class ShortBinderLookup extends ValueBinderLookup<Short> {

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static final ValueBinder DEFAULT = new ValueBinder(SET_SHORT);

  @SuppressWarnings("unchecked")
  public ShortBinderLookup() {
    put(BOOLEAN, shortToBoolean());
    put(TINYINT, shortToByte());
    put(SMALLINT, DEFAULT);
    put(INTEGER, IntBinderLookup.DEFAULT);
    put(BIGINT, LongBinderLookup.DEFAULT);
    put(REAL, FloatBinderLookup.DEFAULT);
    addMultiple(DoubleBinderLookup.DEFAULT, DOUBLE, FLOAT);
    addMultiple(BigDecimalBinderLookup.DEFAULT, NUMERIC, DECIMAL);
    addMultiple(ANY_TO_STRING, VARCHAR, CHAR);
  }

  private static ValueBinder<Object, Boolean> shortToBoolean() {
    return new ValueBinder<>(SET_BOOLEAN, Bool::from);
  }

  private static ValueBinder<Short, Byte> shortToByte() {
    return new ValueBinder<>(SET_BYTE, NumberMethods::convert);
  }


}
