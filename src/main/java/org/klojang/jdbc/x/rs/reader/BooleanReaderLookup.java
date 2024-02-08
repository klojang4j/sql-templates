package org.klojang.jdbc.x.rs.reader;

import org.klojang.convert.Bool;
import org.klojang.jdbc.x.rs.ColumnReader;
import org.klojang.jdbc.x.rs.ColumnReaderLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;


public final class BooleanReaderLookup extends ColumnReaderLookup<Boolean> {

  public BooleanReaderLookup() {
    add(INTEGER, new ColumnReader<>(GET_INT, Bool::from));
    add(SMALLINT, new ColumnReader<>(GET_SHORT, Bool::from));
    add(TINYINT, new ColumnReader<>(GET_BYTE, Bool::from));
    add(BIGINT, new ColumnReader<>(GET_LONG, Bool::from));
    add(REAL, new ColumnReader<>(GET_FLOAT, Bool::from));
    addMultiple(new ColumnReader<>(GET_BOOLEAN),
          BOOLEAN,
          BIT);
    addMultiple(new ColumnReader<>(GET_DOUBLE, Bool::from),
          FLOAT,
          DOUBLE);
    addMultiple(new ColumnReader<>(GET_BIG_DECIMAL, Bool::from),
          NUMERIC,
          DECIMAL);
    addMultiple(new ColumnReader<>(GET_STRING, Bool::from),
          CHAR,
          VARCHAR);
  }

}
