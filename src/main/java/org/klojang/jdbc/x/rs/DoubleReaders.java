package org.klojang.jdbc.x.rs;

import org.klojang.convert.NumberMethods;

import static java.sql.Types.*;

/*
 * Extracts various types of column values from a ResultSet and converts them to double values.
 */
final class DoubleReaders extends ReaderLookup<Double> {

  DoubleReaders() {
    addMultiple(new ResultSetReader<>(ResultSetMethod.GET_DOUBLE), FLOAT, DOUBLE);
    // We don't really need to add the conversion functions (like  Integer::doubleValue)
    // because the compiler can figure this out by itself. But we like to be explicit.
    add(INTEGER, new ResultSetReader<>(ResultSetMethod.GET_INT, Integer::doubleValue));
    add(SMALLINT, new ResultSetReader<>(ResultSetMethod.GET_SHORT, Short::doubleValue));
    add(TINYINT, new ResultSetReader<>(ResultSetMethod.GET_BYTE, Byte::doubleValue));
    add(REAL, new ResultSetReader<>(ResultSetMethod.GET_FLOAT, Float::doubleValue));
    add(BIGINT, new ResultSetReader<>(ResultSetMethod.GET_LONG, Long::doubleValue));
    add(BOOLEAN, new ResultSetReader<>(ResultSetMethod.GET_BOOLEAN, x -> x ? 1.0 : 0));
    addMultiple(
          new ResultSetReader<>(ResultSetMethod.GET_BIG_DECIMAL, NumberMethods::convert),
          NUMERIC,
          DECIMAL);
    add(VARCHAR, new ResultSetReader<>(ResultSetMethod.GET_STRING, NumberMethods::parse));
  }

}
