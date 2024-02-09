package org.klojang.jdbc.x.rs.reader;

import org.klojang.jdbc.x.rs.ColumnReader;
import org.klojang.jdbc.x.rs.ColumnReaderLookup;

import static org.klojang.jdbc.x.rs.ResultSetMethod.GET_STRING;

// This one is special. If the target type is java.lang.String, we don't even bother
// checking what the column type is. We are going to call ResultSet.getString() no
// matter what.
public final class StringReaderLookup implements ColumnReaderLookup<String> {

  private static final ColumnReader<?, String> UNIVERSAL = new ColumnReader<>(GET_STRING);

  public StringReaderLookup() { }

  @Override
  public ColumnReader<?, String> getColumnReader(int columnType) {
    return UNIVERSAL;
  }
}
