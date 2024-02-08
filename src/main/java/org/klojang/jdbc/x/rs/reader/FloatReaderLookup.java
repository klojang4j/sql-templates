package org.klojang.jdbc.x.rs.reader;

import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.rs.ColumnReader;
import org.klojang.jdbc.x.rs.ColumnReaderLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;

public final class FloatReaderLookup extends ColumnReaderLookup<Float> {

  public FloatReaderLookup() {
    add(FLOAT, new ColumnReader<>(GET_FLOAT));
    add(INTEGER, new ColumnReader<>(GET_INT, Integer::floatValue));
    add(SMALLINT, new ColumnReader<>(GET_SHORT, Short::floatValue));
    add(TINYINT, new ColumnReader<>(GET_BYTE, Byte::floatValue));
    add(REAL, new ColumnReader<>(GET_FLOAT, Float::floatValue));
    add(BIGINT, new ColumnReader<>(GET_LONG, Long::floatValue));
    add(BOOLEAN, new ColumnReader<>(GET_BOOLEAN, x -> x ? 1.0F : 0));
    addMultiple(new ColumnReader<>(GET_BIG_DECIMAL, NumberMethods::convert),
          NUMERIC,
          DECIMAL);
    addMultiple(new ColumnReader<>(GET_STRING, NumberMethods::parse),
          VARCHAR,
          CHAR);
  }

}
