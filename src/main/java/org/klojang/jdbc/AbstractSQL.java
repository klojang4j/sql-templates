package org.klojang.jdbc;

import org.klojang.jdbc.x.ps.BeanBinder;
import org.klojang.jdbc.x.ps.MapBinder;
import org.klojang.jdbc.x.sql.SQLInfo;
import org.klojang.templates.NameMapper;
import org.klojang.util.Tuple2;
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
  private final BindInfo bindInfo;

  private final Map<Class<?>, BeanBinder<?>> beanBinders;
  private final Map<Tuple2<Class<?>, NameMapper>, BeanifierFactory<?>> beanifiers;
  private final Map<NameMapper, MappifierFactory> mappifiers;

  AbstractSQL(String sql, BindInfo bindInfo) {
    this.unparsed = sql;
    this.bindInfo = bindInfo;
    // These maps are unlikely to grow beyond one or two entries
    beanBinders = new HashMap<>(4);
    beanifiers = new HashMap<>(4);
    mappifiers = new HashMap<>(4);
  }

  /**
   * Returns the raw, unprocessed, user-provided SQL string, with all named parameters and
   * template variables still in it. For reporting purposes only.
   */
  String unparsed() {
    return unparsed;
  }

  BindInfo bindInfo() {
    return bindInfo;
  }

  BeanBinder<?> getBeanBinder(SQLInfo sqlInfo, Class<?> clazz) {
    BeanBinder<?> binder = beanBinders.get(clazz);
    if (binder == null) {
      binder = new BeanBinder<>(clazz, sqlInfo.parameters(), bindInfo);
      beanBinders.put(clazz, binder);
    }
    return binder;
  }

  MapBinder getMapBinder(SQLInfo sqlInfo) {
    return new MapBinder(sqlInfo.parameters(), bindInfo);
  }

  @SuppressWarnings("unchecked")
  <T> BeanifierFactory<T> getBeanifierFactory(Class<T> clazz, NameMapper mapper) {
    Tuple2<Class<?>, NameMapper> key = Tuple2.of(clazz, mapper);
    BeanifierFactory<T> factory = (BeanifierFactory<T>) beanifiers.get(key);
    if (factory == null) {
      beanifiers.put(key, factory = new BeanifierFactory<>(clazz, mapper));
    }
    return factory;
  }

  @SuppressWarnings("unchecked")
  <T> BeanifierFactory<T> getBeanifierFactory(
        Class<T> clazz,
        Supplier<T> supplier,
        NameMapper mapper) {
    Tuple2<Class<?>, NameMapper> key = Tuple2.of(clazz, mapper);
    BeanifierFactory<T> factory = (BeanifierFactory<T>) beanifiers.get(key);
    if (factory == null) {
      beanifiers.put(key, factory = new BeanifierFactory<>(clazz, supplier, mapper));
    }
    return factory;
  }

  MappifierFactory getMappifierFactory(NameMapper mapper) {
    return mappifiers.computeIfAbsent(mapper, MappifierFactory::new);
  }

}
