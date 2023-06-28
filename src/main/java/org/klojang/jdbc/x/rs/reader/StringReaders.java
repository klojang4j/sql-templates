package org.klojang.jdbc.x.rs.reader;

import org.klojang.jdbc.x.rs.Adapter;
import org.klojang.jdbc.x.rs.ResultSetMethod;
import org.klojang.jdbc.x.rs.ResultSetReader;
import org.klojang.jdbc.x.rs.ResultSetReaderLookup;

import static java.sql.Types.CHAR;
import static java.sql.Types.INTEGER;
import static java.sql.Types.VARCHAR;
import static org.klojang.util.StringMethods.EMPTY_STRING;

public final class StringReaders extends ResultSetReaderLookup<String> {

  private static final Adapter<Object, String> TO_STRING =
      (x, y) -> x == null ? EMPTY_STRING : x.toString();

  public StringReaders() {
    put(VARCHAR, new ResultSetReader<>(ResultSetMethod.GET_STRING));
    put(CHAR, new ResultSetReader<>(ResultSetMethod.GET_STRING));
    put(INTEGER, new ResultSetReader<>(ResultSetMethod.GET_INT, TO_STRING));
  }

}
