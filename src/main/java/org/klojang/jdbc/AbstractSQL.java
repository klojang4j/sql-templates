package org.klojang.jdbc;

import org.klojang.jdbc.x.ps.BeanBinder;
import org.klojang.jdbc.x.ps.MapBinder;
import org.klojang.jdbc.x.sql.SQLInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

abstract sealed class AbstractSQL implements SQL
      permits SimpleSQL, SQLTemplate, SQLSkeleton {

  @SuppressWarnings({"unused"})
  private static final Logger LOG = LoggerFactory.getLogger(AbstractSQLSession.class);

  private final String unparsed;
  private final SessionConfig config;

  @SuppressWarnings("rawtypes")
  private final Map<Class, BeanBinder> binders;
  @SuppressWarnings("rawtypes")
  private final Map<Class, BeanExtractorFactory> factories;

  private MapExtractorFactory mapExtractorFactory;

  AbstractSQL(String sql, SessionConfig config) {
    this.unparsed = sql;
    this.config = config;
    // These maps are unlikely to grow beyond one or two entries (you can't extract
    // _that_ many beans from a single row).
    binders = new HashMap<>();
    factories = new HashMap<>();
  }

  final String unparsed() {
    return unparsed;
  }

  final SessionConfig config() {
    return config;
  }

  @SuppressWarnings("unchecked")
  final <T> BeanBinder<T> getBeanBinder(SQLInfo sqlInfo, Class<T> clazz) {
    return binders.computeIfAbsent(clazz,
          k -> new BeanBinder<>(clazz, sqlInfo.parameters(), config));
  }

  final MapBinder getMapBinder(SQLInfo sqlInfo) {
    return new MapBinder(sqlInfo.parameters(), config);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  final <T> BeanExtractorFactory<T> getBeanExtractorFactory(Class<T> clazz) {
    return factories.computeIfAbsent(clazz, k -> new BeanExtractorFactory<>(k, config));
  }

  @SuppressWarnings("unchecked")
  final <T> BeanExtractorFactory<T> getBeanExtractorFactory(Class<T> clazz,
        Supplier<T> supplier) {
    return factories.computeIfAbsent(clazz,
          k -> new BeanExtractorFactory<>(clazz, supplier, config));
  }

  final MapExtractorFactory getMapExtractorFactory() {
    if (mapExtractorFactory == null) {
      mapExtractorFactory = new MapExtractorFactory(config);
    }
    return mapExtractorFactory;
  }

}
