package org.klojang.jdbc.x.rs.reader;

import org.klojang.convert.Bool;
import org.klojang.jdbc.x.rs.ResultSetReader;
import org.klojang.jdbc.x.rs.ResultSetReaderLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;


public final class BooleanReaders extends ResultSetReaderLookup<Boolean> {

  public BooleanReaders() {
    addMultiple(new ResultSetReader<>(GET_BOOLEAN), BOOLEAN, BIT);
    add(INTEGER, new ResultSetReader<>(GET_INT, Bool::from));
    add(SMALLINT, new ResultSetReader<>(GET_SHORT, Bool::from));
    add(TINYINT, new ResultSetReader<>(GET_BYTE, Bool::from));
    addMultiple(new ResultSetReader<>(GET_DOUBLE, Bool::from), FLOAT, DOUBLE);
    put(BIGINT, new ResultSetReader<>(GET_LONG, Bool::from));
    put(REAL, new ResultSetReader<>(GET_FLOAT, Bool::from));
    addMultiple(new ResultSetReader<>(GET_BIG_DECIMAL, Bool::from), NUMERIC, DECIMAL);
    addMultiple(new ResultSetReader<>(GET_STRING, Bool::from), CHAR, VARCHAR);
  }

}
