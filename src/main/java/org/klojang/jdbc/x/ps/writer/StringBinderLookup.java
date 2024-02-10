package org.klojang.jdbc.x.ps.writer;

import org.klojang.convert.Bool;
import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.ps.ValueBinder;
import org.klojang.jdbc.x.ps.ValueBinderLookup;

import java.math.BigDecimal;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;

public final class StringBinderLookup extends ValueBinderLookup<String> {

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static final ValueBinder DEFAULT = new ValueBinder(SET_STRING);

  @SuppressWarnings("unchecked")
  public StringBinderLookup() {
    // Friendly reminder on how to read this:
    // * if we are dealing with a String value
    // * and the database type is Types.BOOLEAN
    // * then we are going to call PreparedStatement.setBoolean() and we will call
    //   Bool.from(String) to convert the input value to a boolean
    put(BOOLEAN, stringToBool());
    put(INTEGER, stringToInt());
    put(SMALLINT, stringToShort());
    put(TINYINT, stringToByte());
    put(REAL, stringToFloat());
    put(BIGINT, stringToLong());
    addMultiple(stringToDouble(), DOUBLE, FLOAT);
    addMultiple(stringToBigDecimal(), NUMERIC, DECIMAL);
    addMultiple(DEFAULT, VARCHAR, CHAR);
  }

  private static ValueBinder<String, Boolean> stringToBool() {
    return new ValueBinder<>(SET_BOOLEAN, Bool::from);
  }

  private static ValueBinder<String, Byte> stringToByte() {
    return new ValueBinder<>(SET_BYTE, NumberMethods::parse);
  }

  private static ValueBinder<String, Short> stringToShort() {
    return new ValueBinder<>(SET_SHORT, NumberMethods::parse);
  }

  private static ValueBinder<String, Integer> stringToInt() {
    return new ValueBinder<>(SET_INT, NumberMethods::parse);
  }

  private static ValueBinder<String, Long> stringToLong() {
    return new ValueBinder<>(SET_LONG, NumberMethods::parse);
  }

  private static ValueBinder<String, Float> stringToFloat() {
    return new ValueBinder<>(SET_FLOAT, NumberMethods::parse);
  }

  private static ValueBinder<String, Double> stringToDouble() {
    return new ValueBinder<>(SET_DOUBLE, NumberMethods::parse);
  }

  private static ValueBinder<String, BigDecimal> stringToBigDecimal() {
    return new ValueBinder<>(SET_BIG_DECIMAL, NumberMethods::parse);
  }


}
