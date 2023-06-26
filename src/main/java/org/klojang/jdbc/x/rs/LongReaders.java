package org.klojang.jdbc.x.rs;

import org.klojang.convert.NumberMethods;

import static java.sql.Types.*;

final class LongReaders extends ReaderLookup<Long> {

  LongReaders() {
    add(BIGINT, new ResultSetReader<>(ResultSetMethod.GET_LONG));
    add(INTEGER, new ResultSetReader<>(ResultSetMethod.GET_INT));
    add(SMALLINT, new ResultSetReader<>(ResultSetMethod.GET_SHORT));
    add(TINYINT, new ResultSetReader<>(ResultSetMethod.GET_BYTE));
    add(REAL, new ResultSetReader<Float, Long>(ResultSetMethod.GET_FLOAT, NumberMethods::convert));
    addMultiple(new ResultSetReader<>(ResultSetMethod.GET_DOUBLE, NumberMethods::convert),
        FLOAT,
        DOUBLE);
    addMultiple(new ResultSetReader<>(ResultSetMethod.GET_BIG_DECIMAL, NumberMethods::convert),
        NUMERIC,
        DECIMAL);
    addMultiple(new ResultSetReader<>(ResultSetMethod.GET_STRING, NumberMethods::parse), VARCHAR, CHAR);
  }

}
