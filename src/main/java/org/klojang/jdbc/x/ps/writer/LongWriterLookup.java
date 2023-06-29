package org.klojang.jdbc.x.ps.writer;

import org.klojang.convert.Bool;
import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.ps.ColumnWriter;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;

public final class LongWriterLookup extends ColumnWriterLookup<Long> {

  public static final ColumnWriter<Long, Long> DEFAULT = new ColumnWriter<>(SET_LONG);

  public LongWriterLookup() {
    put(BIGINT, DEFAULT);
    put(INTEGER, new ColumnWriter<Long, Integer>(SET_INT, NumberMethods::convert));
    put(SMALLINT, new ColumnWriter<Long, Short>(SET_SHORT, NumberMethods::convert));
    put(TINYINT, new ColumnWriter<Long, Byte>(SET_BYTE, NumberMethods::convert));
    put(BOOLEAN, new ColumnWriter<Long, Boolean>(SET_BOOLEAN, Bool::from));
    put(BIT, new ColumnWriter<Long, Boolean>(SET_BOOLEAN, Bool::from));
    put(VARCHAR, ColumnWriter.ANY_TO_STRING);
    put(CHAR, ColumnWriter.ANY_TO_STRING);
  }

}
