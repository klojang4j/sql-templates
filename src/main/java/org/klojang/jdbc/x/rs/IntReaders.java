package org.klojang.jdbc.x.rs;

import org.klojang.convert.NumberMethods;

import static java.sql.Types.*;

final class IntReaders extends ReaderLookup<Integer> {

  IntReaders() {
    add(INTEGER, new ResultSetReader<>(ResultSetMethod.GET_INT));
    add(SMALLINT, new ResultSetReader<>(ResultSetMethod.GET_SHORT, x -> (int) x));
    add(TINYINT, new ResultSetReader<>(ResultSetMethod.GET_BYTE, x -> (int) x));
    add(BOOLEAN, new ResultSetReader<>(ResultSetMethod.GET_BOOLEAN, x -> x ? 1 : 0));
    add(REAL, new ResultSetReader<>(ResultSetMethod.GET_FLOAT, NumberMethods::convert));
    add(BIGINT, new ResultSetReader<>(ResultSetMethod.GET_LONG, NumberMethods::convert));
    addMultiple(new ResultSetReader<>(ResultSetMethod.GET_DOUBLE, NumberMethods::convert),
        FLOAT,
        DOUBLE);
    addMultiple(new ResultSetReader<>(ResultSetMethod.GET_BIG_DECIMAL, NumberMethods::convert),
        NUMERIC,
        DECIMAL);
    add(VARCHAR, new ResultSetReader<String, Integer>(ResultSetMethod.GET_STRING, NumberMethods::parse));
  }

}
