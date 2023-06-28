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
public class MapChannel<COLUMN_TYPE> implements Channel<Map<String, Object>> {

  public static Map<String, Object> toMap(ResultSet rs, MapChannel[] channels)
        throws Throwable {
    // Allow for some extra data to be inserted into the map by the user
    Map<String, Object> map = HashMap.newHashMap(channels.length + 4);
    for (MapChannel channel : channels) {
      channel.copy(rs, map);
    }
    return map;
  }

  public static MapChannel[] createChannels(ResultSet rs, NameMapper mapper) {
    ResultSetMethodLookup methods = ResultSetMethodLookup.getInstance();
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      int sz = rsmd.getColumnCount();
      MapChannel[] transporters = new MapChannel[sz];
      for (int idx = 0; idx < sz; ++idx) {
        int jdbcIdx = idx + 1; // JDBC is one-based
        int sqlType = rsmd.getColumnType(jdbcIdx);
        ResultSetMethod<?> method = methods.getMethod(sqlType);
        String label = rsmd.getColumnLabel(jdbcIdx);
        String key = mapper.map(label);
        transporters[idx] = new MapChannel<>(method, jdbcIdx, sqlType, key);
      }
      return transporters;
    } catch (SQLException e) {
      throw ExceptionMethods.uncheck(e);
    }
  }

  private final ResultSetMethod<COLUMN_TYPE> method;
  private final int jdbcIdx;
  private final int sqlType;
  private final String key;

  private MapChannel(
        ResultSetMethod<COLUMN_TYPE> method,
        int jdbcIdx,
        int sqlType,
        String key) {
    this.method = method;
    this.jdbcIdx = jdbcIdx;
    this.sqlType = sqlType;
    this.key = key;
  }

  @Override
  public void copy(ResultSet rs, Map<String, Object> map) throws Throwable {
    map.put(key, method.invoke(rs, jdbcIdx));
  }

  @Override
  public int getSqlType() {
    return sqlType;
  }
}
