package org.klojang.jdbc.x.rs;

import org.klojang.convert.NumberMethods;

import static java.sql.Types.*;

/*
 * Extracts various types of column values from a ResultSet and converts them to double values.
 */
class DoubleExtractors extends ExtractorLookup<Double> {

  DoubleExtractors() {
    addMultiple(new RsExtractor<>(RsMethod.GET_DOUBLE), FLOAT, DOUBLE);
    // We don't really need to add the conversion functions (like  Integer::doubleValue)
    // because the compiler can figure this out by itself. But we like to be explicit.
    add(INTEGER, new RsExtractor<>(RsMethod.GET_INT, Integer::doubleValue));
    add(SMALLINT, new RsExtractor<>(RsMethod.GET_SHORT, Short::doubleValue));
    add(TINYINT, new RsExtractor<>(RsMethod.GET_BYTE, Byte::doubleValue));
    add(REAL, new RsExtractor<>(RsMethod.GET_FLOAT, Float::doubleValue));
    add(BIGINT, new RsExtractor<>(RsMethod.GET_LONG, Long::doubleValue));
    add(BOOLEAN, new RsExtractor<>(RsMethod.GET_BOOLEAN, x -> x ? 1.0 : 0));
    addMultiple(new RsExtractor<>(RsMethod.GET_BIG_DECIMAL, NumberMethods::convert),
        NUMERIC,
        DECIMAL);
    add(VARCHAR, new RsExtractor<>(RsMethod.GET_STRING, NumberMethods::parse));
  }

}
