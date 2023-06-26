package org.klojang.jdbc.x.rs;

import org.klojang.convert.Bool;

import static java.sql.Types.*;

final class BooleanReaders extends ReaderLookup<Boolean> {

  BooleanReaders() {
    addMultiple(new ResultSetReader<>(ResultSetMethod.GET_BOOLEAN), BOOLEAN, BIT);
    add(INTEGER, new ResultSetReader<>(ResultSetMethod.GET_INT, Bool::from));
    add(SMALLINT, new ResultSetReader<>(ResultSetMethod.GET_SHORT, Bool::from));
    add(TINYINT, new ResultSetReader<>(ResultSetMethod.GET_BYTE, Bool::from));
    addMultiple(new ResultSetReader<>(ResultSetMethod.GET_DOUBLE, Bool::from),
          FLOAT,
          DOUBLE);
    put(BIGINT, new ResultSetReader<Long, Boolean>(ResultSetMethod.GET_LONG, Bool::from));
    put(REAL, new ResultSetReader<Float, Boolean>(ResultSetMethod.GET_FLOAT, Bool::from));
    addMultiple(new ResultSetReader<>(ResultSetMethod.GET_BIG_DECIMAL, Bool::from),
          NUMERIC,
          DECIMAL);
    addMultiple(new ResultSetReader<>(ResultSetMethod.GET_STRING, Bool::from),
          CHAR,
          VARCHAR);
  }

}
