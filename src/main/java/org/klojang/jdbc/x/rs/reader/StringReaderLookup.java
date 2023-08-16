package org.klojang.jdbc.x.rs.reader;

import org.klojang.jdbc.x.rs.ColumnReader;
import org.klojang.jdbc.x.rs.ColumnReaderLookup;
import org.klojang.util.ObjectMethods;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.rs.ResultSetMethod.GET_INT;
import static org.klojang.jdbc.x.rs.ResultSetMethod.GET_STRING;

public final class StringReaderLookup extends ColumnReaderLookup<String> {

  public StringReaderLookup() {
    put(VARCHAR, new ColumnReader<>(GET_STRING));
    put(CHAR, new ColumnReader<>(GET_STRING));
    put(OTHER, new ColumnReader<>(GET_STRING));
    put(INTEGER, new ColumnReader<>(GET_INT, ObjectMethods::stringify));
  }

}
