package org.klojang.jdbc.x.rs.reader;

import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.rs.ResultSetMethod;
import org.klojang.jdbc.x.rs.ResultSetReader;
import org.klojang.jdbc.x.rs.ResultSetReaderLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;

public final class ShortReaders extends ResultSetReaderLookup<Short> {

  public ShortReaders() {
    add(SMALLINT,
          new ResultSetReader<Short, Short>(ResultSetMethod.GET_SHORT));
    add(TINYINT,
          new ResultSetReader<Byte, Short>(GET_BYTE, NumberMethods::convert));
    add(INTEGER,
          new ResultSetReader<Integer, Short>(GET_INT, NumberMethods::convert));
    add(REAL,
          new ResultSetReader<Float, Short>(GET_FLOAT, NumberMethods::convert));
    add(BIGINT,
          new ResultSetReader<Long, Short>(GET_LONG, NumberMethods::convert));
    addMultiple(new ResultSetReader<>(GET_DOUBLE, NumberMethods::convert),
          FLOAT,
          DOUBLE);
    addMultiple(new ResultSetReader<>(GET_BIG_DECIMAL, NumberMethods::convert),
          NUMERIC,
          DECIMAL);
    addMultiple(new ResultSetReader<>(GET_STRING, NumberMethods::parse),
          VARCHAR,
          CHAR);
  }

}
