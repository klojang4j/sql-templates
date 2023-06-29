package org.klojang.jdbc.x;

import org.klojang.jdbc.KlojangSQLException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public final class JDBC {

  private JDBC() {throw new UnsupportedOperationException();}

  public static String[] getColumnNames(ResultSet rs) {
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      int sz = rsmd.getColumnCount();
      String[] colNames = new String[sz];
      for (int i = 0; i < rsmd.getColumnCount(); ++i) {
        colNames[i] = rsmd.getColumnLabel(i + 1);
      }
      return colNames;
    } catch (SQLException e) {
      throw new KlojangSQLException(e);
    }
  }

}
