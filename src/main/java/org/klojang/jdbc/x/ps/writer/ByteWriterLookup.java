package org.klojang.jdbc.x.ps.writer;

import org.klojang.convert.Bool;
import org.klojang.jdbc.x.ps.ColumnWriter;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.ColumnWriter.ANY_TO_STRING;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.SET_BOOLEAN;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.SET_BYTE;

public final class ByteWriterLookup extends ColumnWriterLookup<Byte> {

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static final ColumnWriter DEFAULT = new ColumnWriter(SET_BYTE);

  @SuppressWarnings("unchecked")
  public ByteWriterLookup() {
    put(TINYINT, DEFAULT);
    put(BOOLEAN, new ColumnWriter<Byte, Boolean>(SET_BOOLEAN, Bool::from));
    put(INTEGER, IntWriterLookup.DEFAULT);
    put(SMALLINT, ShortWriterLookup.DEFAULT);
    put(BIGINT, LongWriterLookup.DEFAULT);
    put(REAL, FloatWriterLookup.DEFAULT);
    addMultiple(DoubleWriterLookup.DEFAULT, DOUBLE, FLOAT);
    addMultiple(BigDecimalWriterLookup.DEFAULT, NUMERIC, DECIMAL);
    addMultiple(ANY_TO_STRING, VARCHAR, CHAR);
  }


}
