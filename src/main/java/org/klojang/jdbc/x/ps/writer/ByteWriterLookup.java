package org.klojang.jdbc.x.ps.writer;

import org.klojang.convert.Bool;
import org.klojang.jdbc.x.ps.ColumnWriter;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;

public final class ByteWriterLookup extends ColumnWriterLookup<Byte> {

  public static final ColumnWriter<Byte, Byte> DEFAULT = new ColumnWriter<>(SET_BYTE);

  public ByteWriterLookup() {
    put(TINYINT, DEFAULT);
    put(INTEGER, IntWriterLookup.DEFAULT);
    put(SMALLINT, new ColumnWriter<>(SET_SHORT));
    put(BIGINT, new ColumnWriter<>(SET_LONG));
    put(REAL, new ColumnWriter<>(SET_FLOAT));
    put(FLOAT, DoubleWriterLookup.DEFAULT);
    put(DOUBLE, DoubleWriterLookup.DEFAULT);
    put(NUMERIC, new ColumnWriter<>(SET_BIG_DECIMAL));
    put(DECIMAL, new ColumnWriter<>(SET_BIG_DECIMAL));
    put(BOOLEAN, new ColumnWriter<Byte, Boolean>(SET_BOOLEAN, Bool::from));
    put(BIT, new ColumnWriter<Byte, Boolean>(SET_BOOLEAN, Bool::from));
    put(VARCHAR, ColumnWriter.ANY_TO_STRING);
    put(CHAR, ColumnWriter.ANY_TO_STRING);
  }


}
