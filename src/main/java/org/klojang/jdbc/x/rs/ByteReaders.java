package org.klojang.jdbc.x.rs;

import org.klojang.convert.NumberMethods;

import static java.sql.Types.*;

final class ByteReaders extends ReaderLookup<Byte> {

  ByteReaders() {
    add(TINYINT, new ResultSetReader<Byte, Byte>(ResultSetMethod.GET_BYTE));
    add(INTEGER, new ResultSetReader<Integer, Byte>(ResultSetMethod.GET_INT, NumberMethods::convert));
    add(SMALLINT, new ResultSetReader<Short, Byte>(ResultSetMethod.GET_SHORT, NumberMethods::convert));
    add(REAL, new ResultSetReader<Float, Byte>(ResultSetMethod.GET_FLOAT, NumberMethods::convert));
    add(BIGINT, new ResultSetReader<Long, Byte>(ResultSetMethod.GET_LONG, NumberMethods::convert));
    addMultiple(new ResultSetReader<>(ResultSetMethod.GET_DOUBLE, NumberMethods::convert),
        FLOAT,
        DOUBLE);
    addMultiple(new ResultSetReader<>(ResultSetMethod.GET_BIG_DECIMAL, NumberMethods::convert),
        NUMERIC,
        DECIMAL);
    addMultiple(new ResultSetReader<>(ResultSetMethod.GET_STRING, NumberMethods::parse), VARCHAR, CHAR);
  }

}
