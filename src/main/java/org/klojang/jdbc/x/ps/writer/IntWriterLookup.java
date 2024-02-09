package org.klojang.jdbc.x.ps.writer;

import org.klojang.convert.Bool;
import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.ps.ColumnWriter;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.ColumnWriter.ANY_TO_STRING;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;

public final class IntWriterLookup extends ColumnWriterLookup<Integer> {

  public static final ColumnWriter DEFAULT = new ColumnWriter(SET_INT);

  public IntWriterLookup() {
    put(BOOLEAN, intToBool());
    put(TINYINT, intToByte());
    put(SMALLINT, intToShort());
    put(INTEGER, DEFAULT);
    addMultiple(ANY_TO_STRING, VARCHAR, CHAR);
  }

  private static ColumnWriter<Object, Boolean> intToBool() {
    return new ColumnWriter<>(SET_BOOLEAN, Bool::from);
  }

  private static ColumnWriter<Integer, Short> intToShort() {
    return new ColumnWriter<>(SET_SHORT, NumberMethods::convert);
  }

  private static ColumnWriter<Integer, Byte> intToByte() {
    return new ColumnWriter<>(SET_BYTE, NumberMethods::convert);
  }


}
