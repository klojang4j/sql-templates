package org.klojang.jdbc.x.ps.writer;

import org.klojang.convert.Bool;
import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.ps.ColumnWriter;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import java.math.BigDecimal;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;

public final class StringWriterLookup extends ColumnWriterLookup<String> {

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static final ColumnWriter DEFAULT = new ColumnWriter(SET_STRING);

  @SuppressWarnings("unchecked")
  public StringWriterLookup() {
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

  private static ColumnWriter<String, Boolean> stringToBool() {
    return new ColumnWriter<>(SET_BOOLEAN, Bool::from);
  }

  private static ColumnWriter<String, Byte> stringToByte() {
    return new ColumnWriter<>(SET_BYTE, NumberMethods::parse);
  }

  private static ColumnWriter<String, Short> stringToShort() {
    return new ColumnWriter<>(SET_SHORT, NumberMethods::parse);
  }

  private static ColumnWriter<String, Integer> stringToInt() {
    return new ColumnWriter<>(SET_INT, NumberMethods::parse);
  }

  private static ColumnWriter<String, Long> stringToLong() {
    return new ColumnWriter<>(SET_LONG, NumberMethods::parse);
  }

  private static ColumnWriter<String, Float> stringToFloat() {
    return new ColumnWriter<>(SET_FLOAT, NumberMethods::parse);
  }

  private static ColumnWriter<String, Double> stringToDouble() {
    return new ColumnWriter<>(SET_DOUBLE, NumberMethods::parse);
  }

  private static ColumnWriter<String, BigDecimal> stringToBigDecimal() {
    return new ColumnWriter<>(SET_BIG_DECIMAL, NumberMethods::parse);
  }


}
