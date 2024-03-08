package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.Utils;
import org.klojang.jdbc.x.ps.BeanBinder;
import org.klojang.jdbc.x.ps.MapBinder;
import org.klojang.jdbc.x.sql.NamedParameter;
import org.klojang.jdbc.x.sql.SQLInfo;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;

import static java.lang.ref.Cleaner.Cleanable;
import static java.util.Collections.singletonMap;
import static org.klojang.check.CommonChecks.*;
import static org.klojang.check.Tag.*;
import static org.klojang.jdbc.x.Strings.RECORD;
import static org.klojang.jdbc.x.Utils.CENTRAL_CLEANER;
import static org.klojang.util.CollectionMethods.collectionToSet;

/**
 * Abstract base class for {@link SQLQuery}, {@link SQLInsert} and {@link SQLUpdate}. A
 * {@code SQLStatement} allows you to bind the named parameters within a SQL statement and
 * then execute it. {@code SQLStatement} instances are not thread-safe. They should be
 * created, operated upon, and closed by one and the same thread. They are not supposed to
 * survive the method in which they are created.
 *
 * @param <T> the {@code SQLStatement} subtype returned by various methods in the
 *       fluent API.
 */
public abstract sealed class SQLStatement<T extends SQLStatement<T>>
      implements AutoCloseable permits SQLQuery, SQLUpdate, SQLInsert {

  private static final String NO_SUCH_PARAM = "no such parameter: \"${arg}\"";
  private static final String DIRTY_INSTANCE = "statement already executed; call reset() first()";

  final AbstractSQLSession session;
  final SQLInfo sqlInfo;

  final List<Object> bindings;
  final Set<NamedParameter> bound;

  private final State state;
  private final Cleanable cleanable;

  private boolean fresh = true;

  SQLStatement(PreparedStatement stmt, AbstractSQLSession session, SQLInfo sqlInfo) {
    this.session = session;
    this.sqlInfo = sqlInfo;
    this.bindings = new ArrayList<>(5);
    this.bound = HashSet.newHashSet(sqlInfo.parameters().size());
    this.state = new State(stmt);
    this.cleanable = CENTRAL_CLEANER.register(this, state);
  }

  /**
   * Binds the specified value to the specified named parameter.
   *
   * @param param the named parameter
   * @param value the value
   * @return this {@code SQLStatement} instance
   */
  public T bind(String param, Object value) {
    Check.that(fresh).is(yes(), DIRTY_INSTANCE);
    Check.notNull(param, PARAM).is(keyIn(), sqlInfo.parameterPositions(), NO_SUCH_PARAM);
    return bind(singletonMap(param, value));
  }

  /**
   * Binds the properties of the specified JavaBean to the named parameters within the SQL
   * statement. Properties that do not correspond to named parameters will tacitly be
   * ignored. The effect of passing anything other than a proper JavaBean (e.g. an
   * {@code Integer}, a {@code String}, or an array) is undefined.
   *
   * @param bean the bean
   * @return this {@code SQLStatement} instance
   */
  @SuppressWarnings("unchecked")
  public T bind(Object bean) {
    Check.that(fresh).is(yes(), DIRTY_INSTANCE);
    Check.notNull(bean, BEAN).then(bindings::add);
    return (T) this;
  }

  /**
   * Binds the components of the specified record to the named parameters within the SQL
   * statement. Record components that do not correspond to named parameters will tacitly
   * be ignored.
   *
   * @param record the record
   * @return this {@code SQLStatement} instance
   */
  @SuppressWarnings("unchecked")
  public T bind(Record record) {
    Check.that(fresh).is(yes(), DIRTY_INSTANCE);
    Check.notNull(record, RECORD).then(bindings::add);
    return (T) this;
  }

  /**
   * Binds the values in the specified map to the named parameters within the SQL
   * statement. Map keys that do not correspond to named parameters will tacitly be
   * ignored.
   *
   * @param map the map whose values to bind to the named parameters within the SQL
   *       statement
   * @return this {@code SQLStatement} instance
   */
  @SuppressWarnings("unchecked")
  public T bind(Map<String, ?> map) {
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

  PreparedStatement stmt() { return state.stmt; }

  abstract void initialize();

  @SuppressWarnings({"unchecked", "rawtypes"})
  void applyBindings(PreparedStatement ps) throws Throwable {
    fresh = false;
    for (Object obj : bindings) {
      AbstractSQL sql = session.getSQL();
      if (obj instanceof Map map) {
        MapBinder binder = sql.getMapBinder(sqlInfo);
        binder.bind(ps, map, bound);
      } else {
        BeanBinder binder = sql.getBeanBinder(sqlInfo, obj.getClass());
        binder.bind(ps, obj);
        bound.addAll(binder.getBoundParameters());
      }
    }
    Check.that(bound.size()).is(eq(), sqlInfo.parameters().size(), unboundParameters());
  }

  /**
   * Releases all resources held by this instance. You cannot reuse the instance after a
   * call to this method.
   */
  @Override
  public void close() {
    cleanable.clean();
  }

  private Supplier<DatabaseException> unboundParameters() {
    Set<String> all = HashSet.newHashSet(sqlInfo.parameters().size());
    all.addAll(sqlInfo.parameterPositions().keySet());
    all.removeAll(collectionToSet(bound, NamedParameter::name));
    String fmt = "SQL contains named parameters that have not been bound yet: %s";
    String msg = String.format(fmt, all);
    return () -> Utils.exception(msg, session.getSQL().unparsed());
  }

  private static class State implements Runnable {

    private final PreparedStatement stmt;

    State(PreparedStatement stmt) { this.stmt = stmt; }

    @Override
    public void run() {
      try {
        stmt.close();
      } catch (SQLException e) {
        // ...
      }
    }
  }
}
