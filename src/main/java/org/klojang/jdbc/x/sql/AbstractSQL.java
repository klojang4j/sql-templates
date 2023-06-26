package org.klojang.jdbc.x.sql;

import org.klojang.check.Check;
import org.klojang.jdbc.*;
import org.klojang.jdbc.x.ps.BeanBinder;
import org.klojang.templates.NameMapper;
import org.klojang.templates.RenderSession;
import org.klojang.util.CollectionMethods;
import org.klojang.util.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static org.klojang.check.CommonExceptions.illegalState;

public abstract sealed class AbstractSQL implements SQL
      permits ParametrizedSQL, SQLTemplate, DynamicSQL {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractSQL.class);

  interface StatementFactory<T extends SQLStatement> {
    T create(Connection con, AbstractSQL sql, SQLInfo sqlInfo);
  }

  private final ReentrantLock lock = new ReentrantLock();

  private final Map<Class<?>, BeanBinder<?>> beanBinders = new HashMap<>(4);
  private final Map<Tuple2<Class<?>, NameMapper>, BeanifierFactory<?>> beanifiers =
        new HashMap<>(4);
  private final Map<NameMapper, MappifierFactory> mappifiers = new HashMap<>(4);


  final String sql;
  final BindInfo bindInfo;

  public AbstractSQL(String sql, BindInfo bindInfo) {
    this.sql = Check.notNull(sql, "sql").ok();
    this.bindInfo = Check.notNull(bindInfo, "bindInfo").ok();
  }

  public SQLQuery prepareQuery(Connection con) {
    return prepare(con, SQLQuery::new);
  }

  public SQLInsert prepareInsert(Connection con) {
    return prepare(con, SQLInsert::new);
  }

  public SQLUpdate prepareUpdate(Connection con) {
    return prepare(con, SQLUpdate::new);
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


  public abstract void unlock();

  abstract <T extends SQLStatement<?>> T prepare(
        Connection con,
        StatementFactory<T> constructor);

  static Supplier<IllegalStateException> sessionNotFinished(RenderSession session) {
    String unset = CollectionMethods.implode(session.getAllUnsetVariables());
    return illegalState("one or more variables have not been set yet: " + unset);
  }

}
