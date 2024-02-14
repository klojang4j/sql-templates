package org.klojang.jdbc.x.rs;

import org.klojang.jdbc.BeanExtractor;
import org.klojang.jdbc.SessionConfig;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.klojang.jdbc.x.rs.PropertyWriter.createWriters;

@SuppressWarnings("rawtypes")
public final class BeanExtractorCache {

  private final Map<BeanExtractorId, PropertyWriter[]> cache0 = new HashMap<>();
  private final Map<BeanExtractorId, RecordFactory> cache1 = new HashMap<>();

  public <T> BeanExtractor<T> getBeanExtractor(Class<T> clazz,
        SessionConfig config,
        ResultSet rs,
        Supplier<T> supplier) throws SQLException {
    ResultSetId resultSetId = new ResultSetId(rs);
    BeanExtractorId extractorId = new BeanExtractorId(clazz, config, resultSetId);
    PropertyWriter[] writers = cache0.get(extractorId);
    if (writers == null) {
      writers = createWriters(rs, clazz, config);
      cache0.put(extractorId, writers);
    }
    return new DefaultBeanExtractor<>(rs, writers, supplier);
  }

  public <T> BeanExtractor<T> getRecordExtractor(Class<T> clazz,
        SessionConfig config,
        ResultSet rs) throws SQLException {
    ResultSetId resultSetId = new ResultSetId(rs);
    BeanExtractorId extractorId = new BeanExtractorId(clazz, config, resultSetId);
    RecordFactory factory = cache1.get(extractorId);
    if (factory == null) {
      factory = new RecordFactory(clazz, rs, config);
      cache1.put(extractorId, factory);
    }
    return new RecordExtractor(rs, factory);
  }


}
