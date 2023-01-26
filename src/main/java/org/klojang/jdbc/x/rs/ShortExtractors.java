package org.klojang.jdbc.x.rs;

import org.klojang.convert.NumberMethods;

import static java.sql.Types.*;

class ShortExtractors extends ExtractorLookup<Short> {

  ShortExtractors() {
    add(SMALLINT, new RsExtractor<Short, Short>(RsMethod.GET_SHORT));
    add(TINYINT, new RsExtractor<Byte, Short>(RsMethod.GET_BYTE, NumberMethods::convert));
    add(INTEGER, new RsExtractor<Integer, Short>(RsMethod.GET_INT, NumberMethods::convert));
    add(REAL, new RsExtractor<Float, Short>(RsMethod.GET_FLOAT, NumberMethods::convert));
    add(BIGINT, new RsExtractor<Long, Short>(RsMethod.GET_LONG, NumberMethods::convert));
    addMultiple(new RsExtractor<>(RsMethod.GET_DOUBLE, NumberMethods::convert),
        FLOAT,
        DOUBLE);
    addMultiple(new RsExtractor<>(RsMethod.GET_BIG_DECIMAL, NumberMethods::convert),
        NUMERIC,
        DECIMAL);
    addMultiple(new RsExtractor<>(RsMethod.GET_STRING, NumberMethods::parse), VARCHAR, CHAR);
  }

}
