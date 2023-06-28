package org.klojang.jdbc.x.rs.reader;

import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.rs.ResultSetReader;
import org.klojang.jdbc.x.rs.ResultSetReaderLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;

public final class DoubleReaders extends ResultSetReaderLookup<Double> {

  public DoubleReaders() {
    addMultiple(new ResultSetReader<>(GET_DOUBLE), FLOAT, DOUBLE);
    // We don't really need to add the conversion functions (like  Integer::doubleValue)
    // because the compiler can figure this out by itself. But we like to be explicit.
    add(INTEGER, new ResultSetReader<>(GET_INT, Integer::doubleValue));
    add(SMALLINT, new ResultSetReader<>(GET_SHORT, Short::doubleValue));
    add(TINYINT, new ResultSetReader<>(GET_BYTE, Byte::doubleValue));
    add(REAL, new ResultSetReader<>(GET_FLOAT, Float::doubleValue));
    add(BIGINT, new ResultSetReader<>(GET_LONG, Long::doubleValue));
    add(BOOLEAN, new ResultSetReader<>(GET_BOOLEAN, x -> x ? 1.0 : 0));
    addMultiple(
          new ResultSetReader<>(GET_BIG_DECIMAL, NumberMethods::convert),
          NUMERIC,
          DECIMAL);
    add(VARCHAR, new ResultSetReader<>(GET_STRING, NumberMethods::parse));
  }

}
