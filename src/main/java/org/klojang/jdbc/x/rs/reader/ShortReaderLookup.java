package org.klojang.jdbc.x.rs.reader;

import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.rs.ResultSetMethod;
import org.klojang.jdbc.x.rs.ColumnReader;
import org.klojang.jdbc.x.rs.ColumnReaderLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;

public final class ShortReaderLookup extends ColumnReaderLookup<Short> {

  public ShortReaderLookup() {
    add(SMALLINT, new ColumnReader<>(ResultSetMethod.GET_SHORT));
    add(TINYINT, new ColumnReader<>(GET_BYTE, NumberMethods::convert));
    add(INTEGER, new ColumnReader<>(GET_INT, NumberMethods::convert));
    add(REAL, new ColumnReader<>(GET_FLOAT, NumberMethods::convert));
    add(BIGINT, new ColumnReader<>(GET_LONG, NumberMethods::convert));
    addMultiple(new ColumnReader<>(GET_DOUBLE, NumberMethods::convert),
          FLOAT,
          DOUBLE);
    addMultiple(new ColumnReader<>(GET_BIG_DECIMAL, NumberMethods::convert),
          NUMERIC,
          DECIMAL);
    addMultiple(new ColumnReader<>(GET_STRING, NumberMethods::parse),
          VARCHAR,
          CHAR);
  }

}
