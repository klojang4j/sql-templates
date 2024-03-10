package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.Err;
import org.klojang.jdbc.x.JDBC;
import org.klojang.jdbc.x.Msg;
import org.klojang.jdbc.x.Utils;
import org.klojang.jdbc.x.ps.BeanBinder;
import org.klojang.jdbc.x.sql.SQLInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.klojang.check.CommonChecks.*;
import static org.klojang.check.CommonExceptions.illegalState;
import static org.klojang.check.CommonExceptions.npe;
import static org.klojang.jdbc.x.Err.ILLEGAL_NULL_VALUE_IN_LIST;
import static org.klojang.jdbc.x.Err.NO_KEYS_WERE_GENERATED;
import static org.klojang.jdbc.x.Strings.*;
import static org.klojang.util.ClassMethods.className;

/**
 * Facilitates the execution of SQL INSERT statements. {@code SQLInsert} instances can be
 * obtained either via {@link SQLSession#prepareInsert()} or via an
 * {@link InsertBuilder}.
 *
 * @see SQLSession#prepareInsert()
 * @see SQL#insert()
 */
@SuppressWarnings("resource")
public final class SQLInsert extends SQLStatement<SQLInsert> {

  private static final Logger LOG = LoggerFactory.getLogger(SQLInsert.class);

  private static final String CANNOT_SET_ID_PROPERTY = "cannot set ID property when key retrieval is disabled";
  private static final String CANNOT_SET_ID_KEY = "cannot set ID key when key retrieval is disabled";
  private static final String KEY_RETRIEVAL_DISABLED = "key retrieval disabled";

  private final List<String> idProperties = new ArrayList<>(5);

  private final boolean retrieveKeys;

  SQLInsert(PreparedStatement stmt,
        AbstractSQLSession sql,
        SQLInfo sqlInfo,
        boolean retrieveKeys) {
    super(stmt, sql, sqlInfo);
    this.retrieveKeys = retrieveKeys;
  }

  /**
   * Binds the values in the specified JavaBean to the named parameters within the SQL
   * statement. Bean properties that do not correspond to named parameters will tacitly be
   * ignored. The effect of passing anything other than a proper JavaBean (e.g. an
   * {@code Integer}, a {@code String}, or an array) is undefined.
   *
   * @param bean the bean whose values to bind to the named parameters within the
   *       SQL statement
   * @return this {@code SQLInsert} instance
   */
  @Override
  public SQLInsert bind(Object bean) {
    super.bind(bean);
    idProperties.add(null);
    return this;
  }

  /**
   * Binds the values in the specified {@code record} to the named parameters within the
   * SQL statement. Record components that do not correspond to named parameters will
   * tacitly be ignored.
   *
   * @param record the {@code record} whose values to bind to the named parameters
   *       within the SQL statement
   * @return this {@code SQLInsert} instance
   */
  @Override
  public SQLInsert bind(Record record) {
    super.bind(record);
    idProperties.add(null);
    return this;
  }

  /**
   * <p>Binds the values in the specified JavaBean to the parameters within the SQL
   * statement. Bean properties that do not correspond to named parameters will tacitly be
   * ignored. The effect of passing anything other than a proper JavaBean (e.g. an
   * {@code Integer}, {@code String} or array) is undefined. The {@code idProperty}
   * argument must be the name of the property corresponding to the primary key column.
   * The database-generated key for that column will be bound back into the bean.
   * Therefore, make sure the bean is modifiable. Notably, do not pass a {@code record}
   * type.
   *
   * <p><i><b>NB</b> JDBC allows for table definitions that generate multiple keys per
   * inserted record, but </i>Klojang JDBC<i> does not support this. If the database
   * generated multiple keys for a single record, </i>Klojang JDBC<i> will assume that the
   * first one was for the primary key column (the column corresponding to the ID
   * property) and ignore the other keys.</i>
   *
   * @param bean the bean whose values to bind to the named parameters within the
   *       SQL statement
   * @param idProperty the name of the property representing the auto-generated
   *       primary key.
   * @return this {@code SQLInsert} instance
   */
  public SQLInsert bind(Object bean, String idProperty) {
    super.bind(bean);
    Check.that(bean).isNot(instanceOf(), Record.class, Err.NOT_MUTABLE, className(bean));
    Check.that(retrieveKeys).is(yes(), CANNOT_SET_ID_PROPERTY);
    Check.notNull(idProperty, ID_PROPERTY).then(idProperties::add);
    return this;
  }

  /**
   * Binds the values in the specified map to the named parameters within the SQL
   * statement. Map keys that do not correspond to named parameters will tacitly be
   * ignored.
   *
   * @param map the map whose values to bind to the named parameters within the SQL
   *       statement
   * @return this {@code SQLInsert} instance
   */
  @Override
  public SQLInsert bind(Map<String, ?> map) {
    super.bind(map);
    idProperties.add(null);
    return this;
  }

  /**
   * Binds the values in the specified map to the named parameters within the SQL
   * statement. Keys that do not correspond to named parameters will be ignored. The
   * {@code idKey} argument must be the name of the map key corresponding to the primary
   * key column. The database-generated key for that column will be bound back into the
   * map. Therefore, make sure the map is modifiable.
   *
   * <p><i><b>NB</b> JDBC allows for table definitions that generate multiple keys per
   * inserted record, but </i>Klojang JDBC<i> does not support this. If the database
   * generated multiple keys for a single record, </i>Klojang JDBC<i> will assume that the
   * first one was for the primary key column (the column corresponding to the ID
   * property) and ignore the other keys.</i>
   *
   * @param map the map whose values to bind to the named parameters within the SQL
   *       statement
   * @param idKey the name of the map key representing the auto-generated primary
   *       key.
   * @return this {@code SQLInsert} instance
   */
  public SQLInsert bind(Map<String, ?> map, String idKey) {
    super.bind(map);
    Check.that(retrieveKeys).is(yes(), CANNOT_SET_ID_KEY);
    Check.notNull(idKey, ID_KEY).then(idProperties::add);
    return this;
  }

  /**
   * Executes the INSERT statement. If the {@code SQLInsert} was configured to
   * {@linkplain SQLSession#prepareInsert(boolean) retrieve auto-generated keys}, and the
   * database did generate a key, this method returns the generated key, else it returns
   * -1.
   *
   * @return the key generated by the database or -1 if retrieval of auto-generated keys
   *       was suppressed, or if no key was generated
   */
  public long execute() {
    try {
      executeStatement();
      if (retrieveKeys) {
        long[] keys = JDBC.getGeneratedKeys(stmt(), 1);
        return keys.length == 0 ? -1 : keys[0];
      }
      return -1;
    } catch (Throwable t) {
      throw Utils.wrap(t, sqlInfo);
    } finally {
      reset();
    }
  }

  /**
   * Executes the INSERT statement. Any JavaBean that was bound using
   * {@link #bind(Object, String) bind(bean, idProperty} will have its ID property set to
   * the key generated by the database. JavaBeans that were bound using
   * {@link #bind(Object) bind(bean)} will remain unmodified. The same applies for maps:
   * an extra key will be added to the map with the value of the primary key column. An
   * {@link IllegalStateException} will be thrown if the database did not generate a key.
   *
   * @return the key generated by the database
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public long executeAndSetID() {
    Check.that(retrieveKeys).is(yes(), illegalState(KEY_RETRIEVAL_DISABLED));
    try {
      executeStatement();
      long[] keys = JDBC.getGeneratedKeys(stmt(), 1);
      Utils.check(keys.length).isNot(zero(), NO_KEYS_WERE_GENERATED);
      long dbKey = keys[0];
      for (int i = 0; i < idProperties.size(); ++i) {
        String idProperty = idProperties.get(i);
        if (idProperty != null) {
          Object obj = bindings.get(i);
          if (obj instanceof Map map) {
            map.put(idProperty, dbKey);
          } else if (!(obj instanceof Record)) {
            JDBC.setID(obj, idProperty, dbKey);
          }
        }
      }
      return dbKey;
    } catch (Throwable t) {
      throw Utils.wrap(t, sqlInfo);
    } finally {
      reset();
    }
  }

  /**
   * Saves the provided beans or records to the database. This method combines the binding
   * and execution phase. Values bound using the various {@code bind()} methods are
   * ignored.
   *
   * @param beans the beans or records to save
   * @param <U> the type of the beans or records
   */
  public <U> void insertBatch(List<U> beans) {
    Check.notNull(beans);
    try {
      for (U bean : beans) {
        addToBatch(bean);
      }
      LOG.trace(Msg.EXECUTING_SQL, sqlInfo.sql());
      stmt().executeBatch();
    } catch (Throwable t) {
      throw Utils.wrap(t, sqlInfo);
    }
  }

  /**
   * Saves the provided beans or records to the database and returns the keys generated by
   * the database. This method combines the binding and execution phase. Values bound
   * using the various {@code bind()} methods are ignored.
   *
   * @param beans the beans or records to save
   * @param <U> the type of the beans or records
   * @return the keys generated by the database
   */
  public <U> long[] insertBatchAndGetIDs(List<U> beans) {
    Check.notNull(beans);
    Check.that(retrieveKeys).is(yes(), illegalState(KEY_RETRIEVAL_DISABLED));
    try {
      for (U bean : beans) {
        addToBatch(bean);
      }
      LOG.trace(Msg.EXECUTING_SQL, sqlInfo.sql());
      stmt().executeBatch();
      return JDBC.getGeneratedKeys(stmt(), beans.size());
    } catch (Throwable t) {
      throw Utils.wrap(t, sqlInfo);
    }
  }

  /**
   * Saves the provided beans to the database to the database and sets the specified ID
   * property in each of them to the key generated by the database. Since this requires
   * the ID property to be mutable, the provided list must by implication not contain
   * {@code record}-type objects. This method combines the binding and execution phase.
   * Values bound using the various {@code bind()} methods are ignored.
   *
   * @param <U> the type of the beans
   * @param idProperty the name of the property corresponding to the primary key
   * @param beans the beans or records to save
   */
  public <U> void insertBatchAndSetIDs(String idProperty, List<U> beans) {
    Check.notNull(idProperty, ID_PROPERTY);
    Check.notNull(beans, BEANS);
    Check.that(retrieveKeys).is(yes(), illegalState(KEY_RETRIEVAL_DISABLED));
    try {
      for (U bean : beans) {
        addToBatch(bean);
      }
      LOG.trace(Msg.EXECUTING_SQL, sqlInfo.sql());
      stmt().executeBatch();
      long[] keys = JDBC.getGeneratedKeys(stmt(), beans.size());
      int i = 0;
      for (U bean : beans) {
        JDBC.setID(bean, idProperty, keys[i++]);
      }
    } catch (Throwable t) {
      throw Utils.wrap(t, sqlInfo);
    }
  }

  @Override
  void initialize() {
    idProperties.clear();
    try {
      stmt().clearParameters();
    } catch (SQLException e) {
      throw Utils.wrap(e, sqlInfo);
    }
  }

  private void executeStatement() throws Throwable {
    LOG.trace(Msg.EXECUTING_SQL, sqlInfo.sql());
    applyBindings(stmt());
    stmt().executeUpdate();
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private <U> void addToBatch(U bean) throws Throwable {
    Check.that(bean).is(notNull(), npe(ILLEGAL_NULL_VALUE_IN_LIST));
    BeanBinder binder = session.getSQL().getBeanBinder(sqlInfo, bean.getClass());
    binder.bind(stmt(), bean);
    stmt().addBatch();
  }

}
