package org.klojang.jdbc.x.ps.writer;

import org.klojang.convert.Bool;
import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.ps.ValueBinder;
import org.klojang.jdbc.x.ps.ValueBinderLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.ValueBinder.ANY_TO_STRING;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;

public final class IntBinderLookup extends ValueBinderLookup<Integer> {

  public static final ValueBinder DEFAULT = new ValueBinder(SET_INT);

  public IntBinderLookup() {
    put(BOOLEAN, intToBool());
    put(TINYINT, intToByte());
    put(SMALLINT, intToShort());
    put(INTEGER, DEFAULT);
    addMultiple(ANY_TO_STRING, VARCHAR, CHAR);
  }

  private static ValueBinder<Object, Boolean> intToBool() {
    return new ValueBinder<>(SET_BOOLEAN, Bool::from);
  }

  private static ValueBinder<Integer, Short> intToShort() {
    return new ValueBinder<>(SET_SHORT, NumberMethods::convert);
  }

  private static ValueBinder<Integer, Byte> intToByte() {
    return new ValueBinder<>(SET_BYTE, NumberMethods::convert);
  }


}
