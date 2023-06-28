package org.klojang.jdbc.x.rs.reader;

import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.rs.ResultSetReader;
import org.klojang.jdbc.x.rs.ResultSetReaderLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;

public final class FloatReaders extends ResultSetReaderLookup<Float> {

  public FloatReaders() {
    add(FLOAT, new ResultSetReader<>(GET_FLOAT));
    add(INTEGER, new ResultSetReader<>(GET_INT, Integer::floatValue));
    add(SMALLINT, new ResultSetReader<>(GET_SHORT, Short::floatValue));
    add(TINYINT, new ResultSetReader<>(GET_BYTE, Byte::floatValue));
    add(REAL, new ResultSetReader<>(GET_FLOAT, Float::floatValue));
    add(BIGINT, new ResultSetReader<>(GET_LONG, Long::floatValue));
    add(BOOLEAN, new ResultSetReader<>(GET_BOOLEAN, x -> x ? 1.0F : 0));
    addMultiple(
          new ResultSetReader<>(GET_BIG_DECIMAL, NumberMethods::convert),
          NUMERIC,
          DECIMAL);
    add(VARCHAR, new ResultSetReader<>(GET_STRING, NumberMethods::parse));
  }

}
