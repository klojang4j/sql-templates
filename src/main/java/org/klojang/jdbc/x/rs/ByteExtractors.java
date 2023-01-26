package org.klojang.jdbc.x.rs;

import org.klojang.convert.NumberMethods;

import static java.sql.Types.*;

class ByteExtractors extends ExtractorLookup<Byte> {

  ByteExtractors() {
    add(TINYINT, new RsExtractor<Byte, Byte>(RsMethod.GET_BYTE));
    add(INTEGER, new RsExtractor<Integer, Byte>(RsMethod.GET_INT, NumberMethods::convert));
    add(SMALLINT, new RsExtractor<Short, Byte>(RsMethod.GET_SHORT, NumberMethods::convert));
    add(REAL, new RsExtractor<Float, Byte>(RsMethod.GET_FLOAT, NumberMethods::convert));
    add(BIGINT, new RsExtractor<Long, Byte>(RsMethod.GET_LONG, NumberMethods::convert));
    addMultiple(new RsExtractor<>(RsMethod.GET_DOUBLE, NumberMethods::convert),
        FLOAT,
        DOUBLE);
    addMultiple(new RsExtractor<>(RsMethod.GET_BIG_DECIMAL, NumberMethods::convert),
        NUMERIC,
        DECIMAL);
    addMultiple(new RsExtractor<>(RsMethod.GET_STRING, NumberMethods::parse), VARCHAR, CHAR);
  }

}
