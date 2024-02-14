package org.klojang.jdbc.x.rs;

import org.klojang.jdbc.MapExtractor;
import org.klojang.jdbc.SessionConfig;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.klojang.jdbc.x.rs.KeyWriter.createWriters;

public final class MapExtractorCache {

  private final Map<MapExtractorId, KeyWriter[]> cache = new HashMap<>();

  public MapExtractor getExtractor(SessionConfig config, ResultSet rs)
        throws SQLException {
    ResultSetId resultSetId = new ResultSetId(rs);
    MapExtractorId extractorId = new MapExtractorId(config, resultSetId);
    KeyWriter[] writers = cache.get(extractorId);
    if (writers == null) {
      writers = createWriters(rs, config);
      cache.put(extractorId, writers);
    }
    return new DefaultMapExtractor(rs, writers);
  }


}
