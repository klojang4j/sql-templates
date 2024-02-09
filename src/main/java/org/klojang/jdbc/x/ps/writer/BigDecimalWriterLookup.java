package org.klojang.jdbc.x.ps.writer;

import org.klojang.convert.Bool;
import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.ps.ColumnWriter;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import java.math.BigDecimal;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.ColumnWriter.ANY_TO_STRING;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;

public final class BigDecimalWriterLookup extends ColumnWriterLookup<BigDecimal> {

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static final ColumnWriter DEFAULT = new ColumnWriter(SET_BIG_DECIMAL);

  @SuppressWarnings("unchecked")
  public BigDecimalWriterLookup() {
    put(BOOLEAN, bdToBool());
    put(TINYINT, bdToByte());
    put(SMALLINT, bdToShort());
    put(INTEGER, bdToInt());
    put(BIGINT, bdToLong());
    put(REAL, bdToFloat());
    addMultiple(bdToDouble(), DOUBLE, FLOAT);
    addMultiple(ANY_TO_STRING, VARCHAR, CHAR);
  }

  private static ColumnWriter<BigDecimal, Boolean> bdToBool() {
    return new ColumnWriter<>(SET_BOOLEAN, Bool::from);
  }

  private static ColumnWriter<BigDecimal, Byte> bdToByte() {
    return new ColumnWriter<>(SET_BYTE, NumberMethods::convert);
  }

  private static ColumnWriter<BigDecimal, Short> bdToShort() {
    return new ColumnWriter<>(SET_SHORT, NumberMethods::convert);
  }

  private static ColumnWriter<BigDecimal, Integer> bdToInt() {
    return new ColumnWriter<>(SET_INT, NumberMethods::convert);
  }

  private static ColumnWriter<BigDecimal, Long> bdToLong() {
    return new ColumnWriter<>(SET_LONG, NumberMethods::convert);
  }

  private static ColumnWriter<BigDecimal, Float> bdToFloat() {
    return new ColumnWriter<>(SET_FLOAT, NumberMethods::convert);
  }

  private static ColumnWriter<BigDecimal, Double> bdToDouble() {
    return new ColumnWriter<>(SET_DOUBLE, NumberMethods::convert);
  }

}
