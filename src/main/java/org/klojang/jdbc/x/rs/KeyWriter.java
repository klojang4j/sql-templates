package org.klojang.jdbc.x.rs;

import org.klojang.jdbc.DatabaseException;
import org.klojang.templates.NameMapper;
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
    // Allow for some extra data to be inserted into the map by the user
    Map<String, Object> map = HashMap.newHashMap(writers.length + 4);
    for (KeyWriter channel : writers) {
      channel.write(resultset, map);
    }
    return map;
  }

  public static KeyWriter[] createWriters(ResultSet resultset, NameMapper mapper) {
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
        String key = mapper.map(label);
        writers[idx] = new KeyWriter<>(method, columnIndex, key);
      }
      return writers;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private final ResultSetMethod<COLUMN_TYPE> method;
  private final int columnIndex;
  private final String key;

  private KeyWriter(ResultSetMethod<COLUMN_TYPE> method, int columnIndex, String key) {
    this.method = method;
    this.columnIndex = columnIndex;
    this.key = key;
  }

  public void write(ResultSet resultset, Map<String, Object> map) throws Throwable {
    Object val = method.invoke(resultset, columnIndex);
    LOG.trace("==> {}: {}", key, val);
    map.put(key, val);
  }

}
