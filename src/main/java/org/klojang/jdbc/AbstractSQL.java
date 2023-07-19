package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.ps.BeanBinder;
import org.klojang.jdbc.x.sql.SQLInfo;
import org.klojang.templates.NameMapper;
import org.klojang.util.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract sealed class AbstractSQL implements SQL
      permits BasicSQL, SQLTemplate, SQLSkeleton {

  @SuppressWarnings({"unused"})
  private static final Logger LOG = LoggerFactory.getLogger(AbstractSQLSession.class);

  private final Map<Class<?>, BeanBinder<?>> beanBinders
        = new HashMap<>(4);
  private final Map<Tuple2<Class<?>, NameMapper>, BeanifierFactory<?>> beanifiers
        = new HashMap<>(4);
  private final Map<NameMapper, MappifierFactory> mappifiers
        = new HashMap<>(4);


  private final String unparsedSQL;
  private final BindInfo bindInfo;

  public AbstractSQL(String sql, BindInfo bindInfo) {
    this.unparsedSQL = sql;
    this.bindInfo = Check.notNull(bindInfo, "bindInfo").ok();
  }

  public String getUnparsedSQL() {
    return unparsedSQL;
  }

  public BindInfo getBindInfo() {
    return bindInfo;
  }

  public BeanBinder<?> getBeanBinder(Class<?> beanClass, SQLInfo sqlInfo) {
    BeanBinder<?> binder = beanBinders.get(beanClass);
    if (binder == null) {
      binder = new BeanBinder<>(beanClass, sqlInfo.parameters(), bindInfo);
      beanBinders.put(beanClass, binder);
    }
    return binder;
  }

  @SuppressWarnings("unchecked")
  public <T> BeanifierFactory<T> getBeanifierFactory(Class<T> clazz, NameMapper mapper) {
    Tuple2<Class<?>, NameMapper> key = Tuple2.of(clazz, mapper);
    BeanifierFactory<T> bf = (BeanifierFactory<T>) beanifiers.get(key);
    if (bf == null) {
      beanifiers.put(key, bf = new BeanifierFactory<>(clazz, mapper));
    }
    return bf;
  }

  @SuppressWarnings("unchecked")
  public <T> BeanifierFactory<T> getBeanifierFactory(
        Class<T> clazz, Supplier<T> supplier, NameMapper mapper) {
    Tuple2<Class<?>, NameMapper> key = Tuple2.of(clazz, mapper);
    BeanifierFactory<T> bf = (BeanifierFactory<T>) beanifiers.get(key);
    if (bf == null) {
      beanifiers.put(key, bf = new BeanifierFactory<>(clazz, supplier, mapper));
    }
    return bf;
  }

  public MappifierFactory getMappifierFactory(NameMapper mapper) {
    return mappifiers.computeIfAbsent(mapper, MappifierFactory::new);
  }

}
