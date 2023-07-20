package org.klojang.jdbc.x.rs;

import org.klojang.templates.NameMapper;
import org.klojang.util.ExceptionMethods;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/*
 * Transports a single value from a ResultSet to a Map<String, Object>
 */
@SuppressWarnings("rawtypes")
public class MapChannel<COLUMN_TYPE> {

  public static Map<String, Object> toMap(ResultSet resultset, MapChannel[] channels)
  throws Throwable {
    // Allow for some extra data to be inserted into the map by the user
    Map<String, Object> map = HashMap.newHashMap(channels.length + 4);
    for (MapChannel channel : channels) {
      channel.copy(resultset, map);
    }
    return map;
  }

  public static MapChannel[] createChannels(ResultSet resultset, NameMapper mapper) {
    ResultSetMethodLookup methods = ResultSetMethodLookup.getInstance();
    try {
      ResultSetMetaData rsmd = resultset.getMetaData();
      int sz = rsmd.getColumnCount();
      MapChannel[] channels = new MapChannel[sz];
      for (int idx = 0; idx < sz; ++idx) {
        int columnIndex = idx + 1; // JDBC is one-based
        int sqlType = rsmd.getColumnType(columnIndex);
        ResultSetMethod<?> method = methods.getMethod(sqlType);
        String label = rsmd.getColumnLabel(columnIndex);
        String key = mapper.map(label);
        channels[idx] = new MapChannel<>(method, columnIndex, key);
      }
      return channels;
    } catch (SQLException e) {
      throw ExceptionMethods.uncheck(e);
    }
  }

  private final ResultSetMethod<COLUMN_TYPE> method;
  private final int columnIndex;
  private final String key;

  private MapChannel(ResultSetMethod<COLUMN_TYPE> method, int columnIndex, String key) {
    this.method = method;
    this.columnIndex = columnIndex;
    this.key = key;
  }

  public void copy(ResultSet resultset, Map<String, Object> map) throws Throwable {
    map.put(key, method.invoke(resultset, columnIndex));
  }

}
