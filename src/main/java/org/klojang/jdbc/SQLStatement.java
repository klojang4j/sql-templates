package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.ps.BeanBinder;
import org.klojang.jdbc.x.ps.MapBinder;
import org.klojang.jdbc.x.sql.AbstractSQLSession;
import org.klojang.jdbc.x.sql.NamedParameter;
import org.klojang.jdbc.x.sql.SQLInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;

import static java.util.Collections.singletonMap;
import static org.klojang.check.CommonChecks.eq;
import static org.klojang.check.CommonChecks.keyIn;
import static org.klojang.check.Tag.*;
import static org.klojang.util.CollectionMethods.collectionToList;

/**
 * Abstract base class for {@link SQLQuery}, {@link SQLInsert} and {@link SQLUpdate}. An
 * {@code SQLStatement} allows you to bind the named parameters within the SQL statement
 * (if present) and then execute it.
 *
 * @param <T> the {@code SQLStatement} subtype returned by various methods in the
 *     fluent API.
 */
public abstract sealed class SQLStatement<T extends SQLStatement<T>>
    implements AutoCloseable permits SQLQuery, SQLUpdate, SQLInsert {

  final Connection con;
  final AbstractSQLSession session;
  final SQLInfo sqlInfo;
  final List<Object> bindings;

  private final Set<NamedParameter> bound;

  SQLStatement(Connection con, AbstractSQLSession session, SQLInfo sqlInfo) {
    this.con = con;
    this.session = session;
    this.sqlInfo = sqlInfo;
    this.bindings = new ArrayList<>(5);
    this.bound = HashSet.newHashSet(sqlInfo.parameters().size());
  }

  /**
   * Binds the specified value to the specified parameter.
   *
   * @param param the parameter
   * @param value the value
   * @return this {@code SQLStatement} instance
   */
  public T bind(String param, Object value) {
    Check.notNull(param, PARAM)
        .is(keyIn(), sqlInfo.parameterPositions(), "no such parameter: \"${arg}\"");
    return bind(singletonMap(param, value));
  }

  /**
   * Binds the values in the specified JavaBean to the parameters within the SQL
   * statement. Bean properties that do not correspond to named parameters will be
   * ignored. The effect of passing anything other than a proper JavaBean (e.g. an
   * {@code Integer}, {@code String} or array) is undefined.
   *
   * @param bean The bean whose values to bind to the named parameters within the SQL
   *     statement
   * @return this {@code SQLStatement} instance
   */
  @SuppressWarnings("unchecked")
  public T bind(Object bean) {
    Check.notNull(bean, BEAN).then(bindings::add);
    return (T) this;
  }

  /**
   * Binds the values in the specified map to the parameters within the SQL statement.
   * Keys that do not correspond to named parameters will be ignored.
   *
   * @param map the map whose values to bind to the named parameters within the SQL
   *     statement
   * @return this {@code SQLStatement} instance
   */
  @SuppressWarnings("unchecked")
  public T bind(Map<String, Object> map) {
    Check.notNull(map, MAP).then(bindings::add);
    return (T) this;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  void applyBindings(PreparedStatement ps) throws Throwable {
    for (Object obj : bindings) {
      if (obj instanceof Map map) {
        MapBinder binder = new MapBinder(
            sqlInfo.parameters(),
            session.getSQL().getBindInfo());
        binder.bindMap(map, ps, bound);
      } else {
        BeanBinder binder = session.getSQL().getBeanBinder(obj.getClass(), sqlInfo);
        binder.bind(obj, ps);
        bound.addAll(binder.getBoundParameters());
      }
    }
    Check.that(bound.size()).is(eq(), sqlInfo.parameters().size(), incompleteBindings());
  }

  void close(PreparedStatement ps) {
    if (ps != null) {
      try {
        ps.close();
      } catch (SQLException e) {
        throw KlojangSQLException.wrap(e, sqlInfo);
      }
    }
  }

  private Supplier<KlojangSQLException> incompleteBindings() {
    Set<NamedParameter> params = HashSet.newHashSet(sqlInfo.parameters().size());
    params.addAll(sqlInfo.parameters());
    params.removeAll(bound);
    List<String> unbound = collectionToList(params, NamedParameter::name);
    String fmt = "some query parameters have not been bound yet: %s";
    return () -> new KlojangSQLException(String.format(fmt, unbound));
  }
}
