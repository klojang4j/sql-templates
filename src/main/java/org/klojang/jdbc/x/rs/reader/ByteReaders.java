package org.klojang.jdbc.x.rs.reader;

import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.rs.ResultSetReader;
import org.klojang.jdbc.x.rs.ResultSetReaderLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;


public final class ByteReaders extends ResultSetReaderLookup<Byte> {

  public ByteReaders() {
    add(TINYINT, new ResultSetReader<>(GET_BYTE));
    add(INTEGER, new ResultSetReader<>(GET_INT, NumberMethods::convert));
    add(SMALLINT, new ResultSetReader<>(GET_SHORT, NumberMethods::convert));
    add(REAL, new ResultSetReader<>(GET_FLOAT, NumberMethods::convert));
    add(BIGINT, new ResultSetReader<>(GET_LONG, NumberMethods::convert));
    addMultiple(new ResultSetReader<>(GET_DOUBLE, NumberMethods::convert), FLOAT, DOUBLE);
    addMultiple(
          new ResultSetReader<>(GET_BIG_DECIMAL, NumberMethods::convert),
          NUMERIC,
          DECIMAL);
    addMultiple(new ResultSetReader<>(GET_STRING, NumberMethods::parse), VARCHAR, CHAR);
  }

}
