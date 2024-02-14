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
  private final Map<Class, BeanBinder> beanBinders;
  @SuppressWarnings("rawtypes")
  private final Map<Class, BeanExtractorFactory> beanifiers;

  AbstractSQL(String sql, SessionConfig config) {
    this.unparsed = sql;
    this.config = config;
    // These maps are unlikely to grow beyond one or two entries (you can't extract that
    // many beans from a single row).
    beanBinders = new HashMap<>();
    beanifiers = new HashMap<>();
  }

  /**
   * Returns the raw, unprocessed, user-provided SQL string, with all named parameters and
   * template variables still in it. For reporting purposes only.
   */
  String unparsed() {
    return unparsed;
  }

  SessionConfig config() {
    return config;
  }

  @SuppressWarnings("unchecked")
  <T> BeanBinder<T> getBeanBinder(SQLInfo sqlInfo, Class<T> clazz) {
    return beanBinders.computeIfAbsent(clazz,
          k -> new BeanBinder<>(clazz, sqlInfo.parameters(), config));
  }

  MapBinder getMapBinder(SQLInfo sqlInfo) {
    return new MapBinder(sqlInfo.parameters(), config);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  <T> BeanExtractorFactory<T> getBeanExtractorFactory(Class<T> clazz) {
    return beanifiers.computeIfAbsent(clazz, k -> new BeanExtractorFactory<>(k, config));
  }

  @SuppressWarnings("unchecked")
  <T> BeanExtractorFactory<T> getBeanExtractorFactory(
        Class<T> clazz,
        Supplier<T> supplier) {
    return beanifiers.computeIfAbsent(clazz,
          k -> new BeanExtractorFactory<>(clazz, supplier, config));
  }

  MapExtractorFactory getMapExtractorFactory() {
    return new MapExtractorFactory(config);
  }

}
