package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.ps.BeanBinder;
import org.klojang.jdbc.x.ps.MapBinder;
import org.klojang.jdbc.x.sql.NamedParameter;
import org.klojang.jdbc.x.sql.SQLInfo;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;

import static java.util.Collections.singletonMap;
import static org.klojang.check.CommonChecks.*;
import static org.klojang.check.Tag.*;
import static org.klojang.util.CollectionMethods.collectionToSet;

/**
 * Abstract base class for {@link SQLQuery}, {@link SQLInsert} and {@link SQLUpdate}. An
 * {@code SQLStatement} allows you to bind the named parameters within the SQL statement
 * (if present) and then execute it.
 *
 * @param <T> the {@code SQLStatement} subtype returned by various methods in the fluent
 * API.
 */
public abstract sealed class SQLStatement<T extends SQLStatement<T>>
      implements AutoCloseable permits SQLQuery, SQLUpdate, SQLInsert {

  private static final String NO_SUCH_PARAM = "no such parameter: \"${arg}\"";
  private static final String DIRTY_INSTANCE = "statement already executed; call reset() first()";

  final PreparedStatement ps;
  final AbstractSQLSession session;
  final SQLInfo sqlInfo;

  final List<Object> bindings;
  final Set<NamedParameter> bound;

  private boolean fresh = true;

  SQLStatement(PreparedStatement ps, AbstractSQLSession session, SQLInfo sqlInfo) {
    this.ps = ps;
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
    Check.that(fresh).is(yes(), DIRTY_INSTANCE);
    Check.notNull(param, PARAM).is(keyIn(), sqlInfo.parameterPositions(), NO_SUCH_PARAM);
    return bind(singletonMap(param, value));
  }

  /**
   * Binds the values in the specified JavaBean to the parameters within the SQL
   * statement. Bean properties that do not correspond to named parameters will be
   * ignored. The effect of passing anything other than a proper JavaBean (e.g. an
   * {@code Integer}, {@code String} or array) is undefined.
   *
   * @param bean The bean whose values to bind to the named parameters within the SQL
   * statement
   * @return this {@code SQLStatement} instance
   */
  @SuppressWarnings("unchecked")
  public T bind(Object bean) {
    Check.that(fresh).is(yes(), DIRTY_INSTANCE);
    Check.notNull(bean, BEAN).then(bindings::add);
    return (T) this;
  }

  /**
   * Binds the values in the specified map to the parameters within the SQL statement.
   * Keys that do not correspond to named parameters will be ignored.
   *
   * @param map the map whose values to bind to the named parameters within the SQL
   * statement
   * @return this {@code SQLStatement} instance
   */
  @SuppressWarnings("unchecked")
  public T bind(Map<String, Object> map) {
    Check.that(fresh).is(yes(), DIRTY_INSTANCE);
    Check.notNull(map, MAP).then(bindings::add);
    return (T) this;
  }

  /**
   * Clears all bindings and allows the statement to be re-executed with new bindings.
   */
  public void reset() {
    bindings.clear();
    bound.clear();
    fresh = true;
    initialize();
  }

  abstract void initialize();

  @SuppressWarnings({"unchecked", "rawtypes"})
  void applyBindings(PreparedStatement ps) throws Throwable {
    fresh = false;
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

  /**
   * Releases all resources held by this instance. You cannot reuse the instance after a
   * call to this method.
   */
  @Override
  public void close() {
    try {
      ps.close();
    } catch (SQLException e) {
      throw KlojangSQLException.wrap(e, sqlInfo);
    }
  }

  private Supplier<KlojangSQLException> incompleteBindings() {
    Set<String> all = new HashSet<>(sqlInfo.parameterPositions().keySet());
    all.removeAll(collectionToSet(bound, NamedParameter::name));
    String fmt = "some query parameters have not been bound yet: %s";
    return () -> new KlojangSQLException(String.format(fmt, all));
  }
}
