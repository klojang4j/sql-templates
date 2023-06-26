package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.ps.BeanBinder;
import org.klojang.jdbc.x.ps.MapBinder;
import org.klojang.jdbc.x.sql.AbstractSQL;
import org.klojang.jdbc.x.sql.NamedParameter;
import org.klojang.jdbc.x.sql.SQLInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static org.klojang.check.CommonChecks.keyIn;
import static org.klojang.util.CollectionMethods.collectionToList;

public abstract class SQLStatement<T extends SQLStatement<T>> implements AutoCloseable {

  final Connection con;
  final AbstractSQL sql;
  final SQLInfo sqlInfo;
  final List<Object> bindables;

  private final Set<NamedParameter> bound;

  SQLStatement(Connection con, AbstractSQL sql, SQLInfo sqlInfo) {
    this.con = con;
    this.sql = sql;
    this.sqlInfo = sqlInfo;
    this.bindables = new ArrayList<>(5);
    this.bound = HashSet.newHashSet(sqlInfo.parameters().size());
  }

  /**
   * Binds the values in the specified JavaBean to the named parameters within the SQL
   * statement. Bean properties that do not correspond to named parameters will be
   * ignored. The effect of passing anything other than a proper JavaBean (e.g. scalars
   * like {@code Integer} or multi-valued objects like {@code Employee[]} or
   * {@code ArrayList}) is undefined. The {@code idProperty} argument must be the name of
   * the property that corresponds to the auto-increment column. The generated value for
   * that column will be bound back into the bean. Of course, the bean or {@code Map}
   * needs to be modifiable in that case. If you don't want the auto-increment column to
   * be bound back into the bean or {@code Map}, just call {@link #bind(Object)}.
   *
   * @param bean The bean whose values to bind to the named parameters within the SQL
   * statement
   * @return This {@code SQLInsert} instance
   */
  @SuppressWarnings("unchecked")
  public T bind(Object bean) {
    Check.notNull(bean, "bean").then(bindables::add);
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T bind(Map<String, ?> map) {
    Check.notNull(map, "map").then(bindables::add);
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T bind(String param, Object value) {
    Check.notNull(param, "param")
          .is(keyIn(), sqlInfo.parameterPositions(), "no such parameter: \"%s\"", param);
    bindables.add(Collections.singletonMap(param, value));
    return (T) this;
  }


  @SuppressWarnings({"unchecked", "rawtypes"})
  <U> void applyBindings(PreparedStatement ps) throws Throwable {
    for (Object obj : bindables) {
      if (obj instanceof Map map) {
        MapBinder binder = new MapBinder(
              sqlInfo.parameters(),
              sql.getBindInfo());
        binder.bindMap(map, ps, bound);
      } else {
        BeanBinder binder = sql.getBeanBinder(obj.getClass(), sqlInfo);
        binder.bind(obj, ps);
        bound.addAll(binder.getBoundParameters());
      }
    }
    if (bound.size() != sqlInfo.parameters().size()) {
      throw notExecutable();
    }
  }

  void close(PreparedStatement ps) {
    try {
      if (ps != null) {
        try {
          ps.close();
        } catch (SQLException e) {
          throw KJSQLException.wrap(e, sqlInfo);
        }
      }
    } finally {
      sql.unlock();
    }
  }

  private KJSQLException notExecutable() {
    Set<NamedParameter> params = HashSet.newHashSet(sqlInfo.parameters().size());
    params.addAll(sqlInfo.parameters());
    params.removeAll(bound);
    List<String> unbound = collectionToList(params, NamedParameter::getName);
    String fmt = "some query parameters have not been bound yet: %s";
    return new KJSQLException(String.format(fmt, unbound));
  }
}
