package org.klojang.jdbc.x.rs.reader;

import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.rs.ResultSetReader;
import org.klojang.jdbc.x.rs.ResultSetReaderLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;

public final class LongReaders extends ResultSetReaderLookup<Long> {

  public LongReaders() {
    add(BIGINT, new ResultSetReader<>(GET_LONG));
    add(INTEGER, new ResultSetReader<>(GET_INT));
    add(SMALLINT, new ResultSetReader<>(GET_SHORT));
    add(TINYINT, new ResultSetReader<>(GET_BYTE));
    add(REAL, new ResultSetReader<Float, Long>(GET_FLOAT, NumberMethods::convert));
    addMultiple(new ResultSetReader<>(GET_DOUBLE, NumberMethods::convert),
          FLOAT,
          DOUBLE);
    addMultiple(new ResultSetReader<>(GET_BIG_DECIMAL, NumberMethods::convert),
          NUMERIC,
          DECIMAL);
    addMultiple(new ResultSetReader<>(GET_STRING, NumberMethods::parse), VARCHAR, CHAR);
  }

}
