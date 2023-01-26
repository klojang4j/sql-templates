package org.klojang.jdbc.x.rs;

import org.klojang.convert.NumberMethods;

import static java.sql.Types.*;

class LongExtractors extends ExtractorLookup<Long> {

  LongExtractors() {
    add(BIGINT, new RsExtractor<>(RsMethod.GET_LONG));
    add(INTEGER, new RsExtractor<>(RsMethod.GET_INT));
    add(SMALLINT, new RsExtractor<>(RsMethod.GET_SHORT));
    add(TINYINT, new RsExtractor<>(RsMethod.GET_BYTE));
    add(REAL, new RsExtractor<Float, Long>(RsMethod.GET_FLOAT, NumberMethods::convert));
    addMultiple(new RsExtractor<>(RsMethod.GET_DOUBLE, NumberMethods::convert),
        FLOAT,
        DOUBLE);
    addMultiple(new RsExtractor<>(RsMethod.GET_BIG_DECIMAL, NumberMethods::convert),
        NUMERIC,
        DECIMAL);
    addMultiple(new RsExtractor<>(RsMethod.GET_STRING, NumberMethods::parse), VARCHAR, CHAR);
  }

}
