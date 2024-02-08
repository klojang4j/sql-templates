package org.klojang.jdbc.x.rs.reader;

import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.rs.ColumnReader;
import org.klojang.jdbc.x.rs.ColumnReaderLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;

public final class IntReaderLookup extends ColumnReaderLookup<Integer> {

  public IntReaderLookup() {
    add(BOOLEAN, new ColumnReader<>(GET_BOOLEAN, x -> x ? 1 : 0));
    add(REAL, new ColumnReader<>(GET_FLOAT, NumberMethods::convert));
    add(BIGINT, new ColumnReader<>(GET_LONG, NumberMethods::convert));
    addMultiple(new ColumnReader<>(GET_INT),
          INTEGER,
          SMALLINT,
          TINYINT);
    addMultiple(new ColumnReader<>(GET_DOUBLE, NumberMethods::convert),
          FLOAT,
          DOUBLE);
    addMultiple(
          new ColumnReader<>(GET_BIG_DECIMAL, NumberMethods::convert),
          NUMERIC,
          DECIMAL);
    addMultiple(new ColumnReader<>(GET_STRING, NumberMethods::parse),
          VARCHAR,
          CHAR);
  }

}
