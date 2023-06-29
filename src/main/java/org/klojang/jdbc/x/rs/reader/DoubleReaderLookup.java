package org.klojang.jdbc.x.rs.reader;

import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.rs.ColumnReader;
import org.klojang.jdbc.x.rs.ColumnReaderLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;

public final class DoubleReaderLookup extends ColumnReaderLookup<Double> {

  public DoubleReaderLookup() {
    addMultiple(new ColumnReader<>(GET_DOUBLE), FLOAT, DOUBLE);
    // We don't really need to add the conversion functions (like  Integer::doubleValue)
    // because the compiler can figure this out by itself. But we like to be explicit.
    add(INTEGER, new ColumnReader<>(GET_INT, Integer::doubleValue));
    add(SMALLINT, new ColumnReader<>(GET_SHORT, Short::doubleValue));
    add(TINYINT, new ColumnReader<>(GET_BYTE, Byte::doubleValue));
    add(REAL, new ColumnReader<>(GET_FLOAT, Float::doubleValue));
    add(BIGINT, new ColumnReader<>(GET_LONG, Long::doubleValue));
    add(BOOLEAN, new ColumnReader<>(GET_BOOLEAN, x -> x ? 1.0 : 0));
    addMultiple(
          new ColumnReader<>(GET_BIG_DECIMAL, NumberMethods::convert),
          NUMERIC,
          DECIMAL);
    add(VARCHAR, new ColumnReader<>(GET_STRING, NumberMethods::parse));
  }

}
