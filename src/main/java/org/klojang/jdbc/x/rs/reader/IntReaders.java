package org.klojang.jdbc.x.rs.reader;

import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.rs.ResultSetReader;
import org.klojang.jdbc.x.rs.ResultSetReaderLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;

public final class IntReaders extends ResultSetReaderLookup<Integer> {

  public IntReaders() {
    add(INTEGER, new ResultSetReader<>(GET_INT));
    add(SMALLINT, new ResultSetReader<>(GET_SHORT, x -> (int) x));
    add(TINYINT, new ResultSetReader<>(GET_BYTE, x -> (int) x));
    add(BOOLEAN, new ResultSetReader<>(GET_BOOLEAN, x -> x ? 1 : 0));
    add(REAL, new ResultSetReader<>(GET_FLOAT, NumberMethods::convert));
    add(BIGINT, new ResultSetReader<>(GET_LONG, NumberMethods::convert));
    addMultiple(new ResultSetReader<>(GET_DOUBLE, NumberMethods::convert), FLOAT, DOUBLE);
    addMultiple(
          new ResultSetReader<>(GET_BIG_DECIMAL, NumberMethods::convert),
          NUMERIC,
          DECIMAL);
    add(VARCHAR, new ResultSetReader<>(GET_STRING, NumberMethods::parse));
  }

}
