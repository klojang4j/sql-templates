package org.klojang.jdbc.x.rs;

import org.klojang.jdbc.CustomReader;
import org.klojang.jdbc.DatabaseException;
import org.klojang.jdbc.SessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


/**
 * Reads a single column in a ResultSet as the value for a Map key.
 *
 * @param <COLUMN_TYPE>
 */
@SuppressWarnings("rawtypes")
public final class KeyWriter<COLUMN_TYPE> {

  private static final Logger LOG = LoggerFactory.getLogger(KeyWriter.class);

  public static Map<String, Object> toMap(ResultSet resultset, KeyWriter[] writers)
        throws Throwable {
    // Allow for some extra data to be inserted by the user
    Map<String, Object> map = HashMap.newHashMap(writers.length + 4);
    for (KeyWriter writer : writers) {
      map.put(writer.key, writer.read(resultset));
    }
    return map;
  }

  public static KeyWriter[] createWriters(ResultSet resultset, SessionConfig config) {
    ResultSetMethodLookup methods = ResultSetMethodLookup.getInstance();
    try {
      ResultSetMetaData rsmd = resultset.getMetaData();
      int sz = rsmd.getColumnCount();
      KeyWriter[] writers = new KeyWriter[sz];
      for (int idx = 0; idx < sz; ++idx) {
        int columnIndex = idx + 1; // JDBC is one-based
        int sqlType = rsmd.getColumnType(columnIndex);
        String label = rsmd.getColumnLabel(columnIndex);
        String key = config.getColumnToPropertyMapper().map(label);
        var customReader = config.getCustomReader(Map.class, key, Object.class, sqlType);
        if (customReader == null) {
          ResultSetMethod<?> method = methods.getMethod(sqlType);
          writers[idx] = new KeyWriter<>(method, columnIndex, key);
        } else {
          writers[idx] = new KeyWriter<>(customReader, columnIndex, key);
        }
      }
      return writers;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private final ResultSetMethod<COLUMN_TYPE> method;
  private final CustomReader customReader;
  private final int columnIndex;
  private final String key;

  private KeyWriter(ResultSetMethod<COLUMN_TYPE> method, int columnIndex, String key) {
    this.method = method;
    this.columnIndex = columnIndex;
    this.key = key;
    this.customReader = null;
  }

  private KeyWriter(CustomReader customReader, int columnIndex, String key) {
    this.customReader = customReader;
    this.columnIndex = columnIndex;
    this.key = key;
    this.method = null;
  }

  private Object read(ResultSet resultSet) throws SQLException {
    final Object val;
    if (customReader == null) {
      val = method.invoke(resultSet, columnIndex);
      LOG.trace("==> {}: {}", key, val);
    } else {
      val = customReader.getValue(resultSet, columnIndex);
      LOG.trace("==> {}: {} (using custom reader)", key, val);
    }
    return val;
  }

}
