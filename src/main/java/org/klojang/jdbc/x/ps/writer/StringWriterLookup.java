package org.klojang.jdbc.x.ps.writer;

import org.klojang.convert.Bool;
import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;
import org.klojang.jdbc.x.ps.PreparedStatementMethod;
import org.klojang.jdbc.x.ps.ColumnWriter;

import java.math.BigDecimal;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;

public final class StringWriterLookup extends ColumnWriterLookup<String> {

  public static final ColumnWriter<String, String> DEFAULT = new ColumnWriter<>(PreparedStatementMethod.SET_STRING);

  public StringWriterLookup() {
    put(VARCHAR, DEFAULT);
    put(CHAR, DEFAULT);
    put(INTEGER, new ColumnWriter<String, Integer>(SET_INT, NumberMethods::parse));
    put(SMALLINT, new ColumnWriter<String, Short>(SET_SHORT, NumberMethods::parse));
    put(TINYINT, new ColumnWriter<String, Byte>(SET_BYTE, NumberMethods::parse));
    put(BIGINT, new ColumnWriter<String, Long>(SET_LONG, NumberMethods::parse));
    put(NUMERIC,
          new ColumnWriter<String, BigDecimal>(SET_BIG_DECIMAL, NumberMethods::parse));
    put(REAL, new ColumnWriter<String, Float>(SET_FLOAT, NumberMethods::parse));
    put(FLOAT, new ColumnWriter<String, Double>(SET_DOUBLE, NumberMethods::parse));
    put(DOUBLE, new ColumnWriter<String, Double>(SET_DOUBLE, NumberMethods::parse));
    put(BOOLEAN, new ColumnWriter<String, Boolean>(SET_BOOLEAN, Bool::from));
    put(BIT, new ColumnWriter<Integer, Boolean>(SET_BOOLEAN, Bool::from));
  }


}
