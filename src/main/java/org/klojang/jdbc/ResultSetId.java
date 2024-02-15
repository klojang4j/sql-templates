package org.klojang.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

final class ResultSetId {

  private final String[] columns;
  private final int[] types;

  ResultSetId(ResultSet rs) throws SQLException {
    ResultSetMetaData rsmd = rs.getMetaData();
    int len = rsmd.getColumnCount();
    columns = new String[len];
    types = new int[len];
    for (int i = 0; i < len; ++i) {
      columns[i] = rsmd.getColumnLabel(i + 1);
      types[i] = rsmd.getColumnType(i + 1);
    }
  }

  @Override
  public int hashCode() {
    int hash = Arrays.hashCode(columns);
    return (hash * 31) + Arrays.hashCode(types);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    ResultSetId other = (ResultSetId) obj;
    return Arrays.equals(types, other.types) && Arrays.equals(columns, other.columns);
  }
}
