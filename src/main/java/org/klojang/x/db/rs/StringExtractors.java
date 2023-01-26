package org.klojang.x.db.rs;

import static java.sql.Types.CHAR;
import static java.sql.Types.INTEGER;
import static java.sql.Types.VARCHAR;
import static org.klojang.util.StringMethods.EMPTY_STRING;
import static org.klojang.x.db.rs.RsMethod.GET_INT;
import static org.klojang.x.db.rs.RsMethod.GET_STRING;

class StringExtractors extends ExtractorLookup<String> {

  private static final Adapter<Object, String> TO_STRING =
      (x, y) -> x == null ? EMPTY_STRING : x.toString();

  StringExtractors() {
    put(VARCHAR, new RsExtractor<>(GET_STRING));
    put(CHAR, new RsExtractor<>(GET_STRING));
    put(INTEGER, new RsExtractor<>(GET_INT, TO_STRING));
  }

}
