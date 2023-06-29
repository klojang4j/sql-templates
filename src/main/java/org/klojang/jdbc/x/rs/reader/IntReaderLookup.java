package org.klojang.jdbc.x.rs.reader;

import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.rs.ColumnReader;
import org.klojang.jdbc.x.rs.ColumnReaderLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;

public final class IntReaderLookup extends ColumnReaderLookup<Integer> {

  public IntReaderLookup() {
    add(INTEGER, new ColumnReader<>(GET_INT));
    add(SMALLINT, new ColumnReader<>(GET_SHORT, x -> (int) x));
    add(TINYINT, new ColumnReader<>(GET_BYTE, x -> (int) x));
    add(BOOLEAN, new ColumnReader<>(GET_BOOLEAN, x -> x ? 1 : 0));
    add(REAL, new ColumnReader<>(GET_FLOAT, NumberMethods::convert));
    add(BIGINT, new ColumnReader<>(GET_LONG, NumberMethods::convert));
    addMultiple(new ColumnReader<>(GET_DOUBLE, NumberMethods::convert), FLOAT, DOUBLE);
    addMultiple(
          new ColumnReader<>(GET_BIG_DECIMAL, NumberMethods::convert),
          NUMERIC,
          DECIMAL);
    add(VARCHAR, new ColumnReader<>(GET_STRING, NumberMethods::parse));
  }

}
