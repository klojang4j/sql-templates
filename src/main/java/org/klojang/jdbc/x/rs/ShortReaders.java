package org.klojang.jdbc.x.rs;

import org.klojang.convert.NumberMethods;

import static java.sql.Types.*;

final class ShortReaders extends ReaderLookup<Short> {

  ShortReaders() {
    add(SMALLINT, new ResultSetReader<Short, Short>(ResultSetMethod.GET_SHORT));
    add(TINYINT, new ResultSetReader<Byte, Short>(ResultSetMethod.GET_BYTE, NumberMethods::convert));
    add(INTEGER, new ResultSetReader<Integer, Short>(ResultSetMethod.GET_INT, NumberMethods::convert));
    add(REAL, new ResultSetReader<Float, Short>(ResultSetMethod.GET_FLOAT, NumberMethods::convert));
    add(BIGINT, new ResultSetReader<Long, Short>(ResultSetMethod.GET_LONG, NumberMethods::convert));
    addMultiple(new ResultSetReader<>(ResultSetMethod.GET_DOUBLE, NumberMethods::convert),
        FLOAT,
        DOUBLE);
    addMultiple(new ResultSetReader<>(ResultSetMethod.GET_BIG_DECIMAL, NumberMethods::convert),
        NUMERIC,
        DECIMAL);
    addMultiple(new ResultSetReader<>(ResultSetMethod.GET_STRING, NumberMethods::parse), VARCHAR, CHAR);
  }

}
