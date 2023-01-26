package org.klojang.jdbc.x.rs;

import static java.sql.Types.CHAR;
import static java.sql.Types.INTEGER;
import static java.sql.Types.VARCHAR;
import static org.klojang.util.StringMethods.EMPTY_STRING;

class StringExtractors extends ExtractorLookup<String> {

  private static final Adapter<Object, String> TO_STRING =
      (x, y) -> x == null ? EMPTY_STRING : x.toString();

  StringExtractors() {
    put(VARCHAR, new RsExtractor<>(RsMethod.GET_STRING));
    put(CHAR, new RsExtractor<>(RsMethod.GET_STRING));
    put(INTEGER, new RsExtractor<>(RsMethod.GET_INT, TO_STRING));
  }

}
