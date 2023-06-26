package org.klojang.jdbc.x.rs;

import static java.sql.Types.CHAR;
import static java.sql.Types.INTEGER;
import static java.sql.Types.VARCHAR;
import static org.klojang.util.StringMethods.EMPTY_STRING;

final class StringReaders extends ReaderLookup<String> {

  private static final Adapter<Object, String> TO_STRING =
      (x, y) -> x == null ? EMPTY_STRING : x.toString();

  StringReaders() {
    put(VARCHAR, new ResultSetReader<>(ResultSetMethod.GET_STRING));
    put(CHAR, new ResultSetReader<>(ResultSetMethod.GET_STRING));
    put(INTEGER, new ResultSetReader<>(ResultSetMethod.GET_INT, TO_STRING));
  }

}
