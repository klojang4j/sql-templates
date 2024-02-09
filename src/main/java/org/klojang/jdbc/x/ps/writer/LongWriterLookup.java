package org.klojang.jdbc.x.ps.writer;

import org.klojang.convert.Bool;
import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.ps.ColumnWriter;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.ColumnWriter.ANY_TO_STRING;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;

public final class LongWriterLookup extends ColumnWriterLookup<Long> {

  public static final ColumnWriter<Long, Long> DEFAULT = new ColumnWriter<>(SET_LONG);

  public LongWriterLookup() {
    put(BOOLEAN, longToBoolean());
    put(TINYINT, longToByte());
    put(SMALLINT, longToShort());
    put(INTEGER, longToInt());
    put(BIGINT, DEFAULT);
    addMultiple(ANY_TO_STRING, VARCHAR, CHAR);
  }

  private static ColumnWriter<Long, Boolean> longToBoolean() {
    return new ColumnWriter<>(SET_BOOLEAN, Bool::from);
  }

  private static ColumnWriter<Long, Byte> longToByte() {
    return new ColumnWriter<>(SET_BYTE, NumberMethods::convert);
  }

  private static ColumnWriter<Long, Short> longToShort() {
    return new ColumnWriter<>(SET_SHORT, NumberMethods::convert);
  }

  private static ColumnWriter<Long, Integer> longToInt() {
    return new ColumnWriter<>(SET_INT, NumberMethods::convert);
  }

}
