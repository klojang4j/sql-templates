package org.klojang.jdbc.x.rs;

import org.klojang.convert.NumberMethods;

import static java.sql.Types.*;

class IntExtractors extends ExtractorLookup<Integer> {

  IntExtractors() {
    add(INTEGER, new RsExtractor<>(RsMethod.GET_INT));
    add(SMALLINT, new RsExtractor<>(RsMethod.GET_SHORT, x -> (int) x));
    add(TINYINT, new RsExtractor<>(RsMethod.GET_BYTE, x -> (int) x));
    add(BOOLEAN, new RsExtractor<>(RsMethod.GET_BOOLEAN, x -> x ? 1 : 0));
    add(REAL, new RsExtractor<>(RsMethod.GET_FLOAT, NumberMethods::convert));
    add(BIGINT, new RsExtractor<>(RsMethod.GET_LONG, NumberMethods::convert));
    addMultiple(new RsExtractor<>(RsMethod.GET_DOUBLE, NumberMethods::convert),
        FLOAT,
        DOUBLE);
    addMultiple(new RsExtractor<>(RsMethod.GET_BIG_DECIMAL, NumberMethods::convert),
        NUMERIC,
        DECIMAL);
    add(VARCHAR, new RsExtractor<String, Integer>(RsMethod.GET_STRING, NumberMethods::parse));
  }

}
