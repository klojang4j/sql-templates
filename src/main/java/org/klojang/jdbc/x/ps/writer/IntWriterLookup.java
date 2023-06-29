package org.klojang.jdbc.x.ps.writer;

import org.klojang.convert.Bool;
import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.ps.PreparedStatementMethod;
import org.klojang.jdbc.x.ps.ColumnWriter;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;

public final class IntWriterLookup extends ColumnWriterLookup<Integer> {

  public static final ColumnWriter<Integer, Integer> DEFAULT = new ColumnWriter<>(PreparedStatementMethod.SET_INT);

  public IntWriterLookup() {
    put(INTEGER, DEFAULT);
    put(SMALLINT, new ColumnWriter<Integer, Short>(SET_SHORT, NumberMethods::convert));
    put(TINYINT, new ColumnWriter<Integer, Byte>(SET_BYTE, NumberMethods::convert));
    putMultiple(new ColumnWriter<>(SET_BOOLEAN, Bool::from), BOOLEAN, BIT);
    put(VARCHAR, ColumnWriter.ANY_TO_STRING);
    put(CHAR, ColumnWriter.ANY_TO_STRING);
  }


}
