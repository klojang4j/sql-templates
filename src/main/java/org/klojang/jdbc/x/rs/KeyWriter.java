package org.klojang.jdbc.x.rs;

import org.klojang.jdbc.DatabaseException;
import org.klojang.jdbc.SessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.klojang.jdbc.SessionConfig.CustomReader;


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
    // Allow for some extra data to be inserted into the map by the user
    Map<String, Object> map = HashMap.newHashMap(writers.length + 4);
    for (KeyWriter channel : writers) {
      channel.write(resultset, map);
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
        ResultSetMethod<?> method = methods.getMethod(sqlType);
        String label = rsmd.getColumnLabel(columnIndex);
        String key = config.getColumnToPropertyMapper().map(label);
        CustomReader custom = config.getCustomReader(HashMap.class,
              key,
              Object.class,
              sqlType);
        if (custom == null) {
          writers[idx] = new KeyWriter<>(method, columnIndex, key);
        } else {
          writers[idx] = new KeyWriter<>(custom, columnIndex, key);
        }
      }
      return writers;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private final ResultSetMethod<COLUMN_TYPE> method;
  private final CustomReader custom;
  private final int columnIndex;
  private final String key;

  private KeyWriter(ResultSetMethod<COLUMN_TYPE> method, int columnIndex, String key) {
    this.method = method;
    this.columnIndex = columnIndex;
    this.key = key;
    this.custom = null;
  }

  private KeyWriter(CustomReader custom, int columnIndex, String key) {
    this.custom = custom;
    this.columnIndex = columnIndex;
    this.key = key;
    this.method = null;
  }

  void write(ResultSet resultset, Map<String, Object> map) throws SQLException {
    final Object val;
    if (custom == null) {
      val = method.invoke(resultset, columnIndex);
      LOG.trace("==> {}: {}", key, val);
    } else {
      val = custom.getValue(resultset, columnIndex);
      LOG.trace("==> {}: {} (using custom reader)", key, val);
    }
    map.put(key, val);
  }

}
