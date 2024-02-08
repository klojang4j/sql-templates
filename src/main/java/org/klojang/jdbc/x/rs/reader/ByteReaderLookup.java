package org.klojang.jdbc.x.rs.reader;

import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.rs.ColumnReader;
import org.klojang.jdbc.x.rs.ColumnReaderLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;

public final class ByteReaderLookup extends ColumnReaderLookup<Byte> {

  public ByteReaderLookup() {
    add(TINYINT, new ColumnReader<>(GET_BYTE));
    add(INTEGER, new ColumnReader<>(GET_INT, NumberMethods::convert));
    add(SMALLINT, new ColumnReader<>(GET_SHORT, NumberMethods::convert));
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
