package org.klojang.jdbc.x.rs.reader;

import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.rs.ColumnReader;
import org.klojang.jdbc.x.rs.ColumnReaderLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;

public final class LongReaderLookup extends ColumnReaderLookup<Long> {

  public LongReaderLookup() {
    add(BIGINT, new ColumnReader<>(GET_LONG));
    add(INTEGER, new ColumnReader<>(GET_INT));
    add(SMALLINT, new ColumnReader<>(GET_SHORT));
    add(TINYINT, new ColumnReader<>(GET_BYTE));
    add(REAL, new ColumnReader<Float, Long>(GET_FLOAT, NumberMethods::convert));
    addMultiple(new ColumnReader<>(GET_DOUBLE, NumberMethods::convert),
          FLOAT,
          DOUBLE);
    addMultiple(new ColumnReader<>(GET_BIG_DECIMAL, NumberMethods::convert),
          NUMERIC,
          DECIMAL);
    addMultiple(new ColumnReader<>(GET_STRING, NumberMethods::parse), VARCHAR, CHAR);
  }

}
