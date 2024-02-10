package org.klojang.jdbc.x.ps.writer;

import org.klojang.convert.Bool;
import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.ps.ValueBinder;
import org.klojang.jdbc.x.ps.ValueBinderLookup;

import java.math.BigDecimal;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.ValueBinder.ANY_TO_STRING;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;

public final class BigDecimalBinderLookup extends ValueBinderLookup<BigDecimal> {

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static final ValueBinder DEFAULT = new ValueBinder(SET_BIG_DECIMAL);

  @SuppressWarnings("unchecked")
  public BigDecimalBinderLookup() {
    put(BOOLEAN, bdToBool());
    put(TINYINT, bdToByte());
    put(SMALLINT, bdToShort());
    put(INTEGER, bdToInt());
    put(BIGINT, bdToLong());
    put(REAL, bdToFloat());
    // Friendly reminder on how to read this:
    // * if we are dealing with a BigDecimal value
    // * and the database type is either Types.DOUBLE or Types.FLOAT
    // * then we are going to call PreparedStatement.setBigDecimal() and we will call
    //   NumberMethods.convert the BigDecimal to a double
    addMultiple(bdToDouble(), DOUBLE, FLOAT);
    addMultiple(bdToString(), VARCHAR, CHAR);
  }

  private static ValueBinder<BigDecimal, Boolean> bdToBool() {
    return new ValueBinder<>(SET_BOOLEAN, Bool::from);
  }

  private static ValueBinder<BigDecimal, Byte> bdToByte() {
    return new ValueBinder<>(SET_BYTE, NumberMethods::convert);
  }

  private static ValueBinder<BigDecimal, Short> bdToShort() {
    return new ValueBinder<>(SET_SHORT, NumberMethods::convert);
  }

  private static ValueBinder<BigDecimal, Integer> bdToInt() {
    return new ValueBinder<>(SET_INT, NumberMethods::convert);
  }

  private static ValueBinder<BigDecimal, Long> bdToLong() {
    return new ValueBinder<>(SET_LONG, NumberMethods::convert);
  }

  private static ValueBinder<BigDecimal, Float> bdToFloat() {
    return new ValueBinder<>(SET_FLOAT, NumberMethods::convert);
  }

  private static ValueBinder<BigDecimal, Double> bdToDouble() {
    return new ValueBinder<>(SET_DOUBLE, NumberMethods::convert);
  }

  private static ValueBinder<BigDecimal, String> bdToString() {
    return new ValueBinder<>(SET_STRING, BigDecimal::toPlainString);
  }


}
