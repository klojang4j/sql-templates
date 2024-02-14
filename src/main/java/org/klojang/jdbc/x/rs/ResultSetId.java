package org.klojang.jdbc.x.rs;

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
    int hash = 1;
    for (int i = 0; i < types.length; ++i) {
      hash = (hash * 31) + types[i];
      hash = (hash * 31) + columns[i].hashCode();
    }
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    ResultSetId other = (ResultSetId) obj;
    return Arrays.equals(types, other.types) && equals(columns, other.columns);
  }

  private static boolean equals(String[] a, String[] b) {
    if (a.length == b.length) {
      for (int i = 0; i < a.length; ++i) {
        if (!a[i].equals(b[i])) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

}
