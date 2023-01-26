package org.klojang.jdbc.x.rs;

import org.klojang.convert.Bool;

import static java.sql.Types.*;

class BooleanExtractors extends ExtractorLookup<Boolean> {

  BooleanExtractors() {
    addMultiple(new RsExtractor<>(RsMethod.GET_BOOLEAN), BOOLEAN, BIT);
    add(INTEGER, new RsExtractor<>(RsMethod.GET_INT, Bool::from));
    add(SMALLINT, new RsExtractor<>(RsMethod.GET_SHORT, Bool::from));
    add(TINYINT, new RsExtractor<>(RsMethod.GET_BYTE, Bool::from));
    addMultiple(new RsExtractor<>(RsMethod.GET_DOUBLE, Bool::from), FLOAT, DOUBLE);
    put(BIGINT, new RsExtractor<Long, Boolean>(RsMethod.GET_LONG, Bool::from));
    put(REAL, new RsExtractor<Float, Boolean>(RsMethod.GET_FLOAT, Bool::from));
    addMultiple(new RsExtractor<>(RsMethod.GET_BIG_DECIMAL, Bool::from), NUMERIC, DECIMAL);
    addMultiple(new RsExtractor<>(RsMethod.GET_STRING, Bool::from), CHAR, VARCHAR);
  }

}
