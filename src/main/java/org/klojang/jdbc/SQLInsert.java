package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.invoke.Setter;
import org.klojang.invoke.SetterFactory;
import org.klojang.jdbc.x.JDBC;
import org.klojang.jdbc.x.sql.AbstractSQLSession;
import org.klojang.jdbc.x.sql.SQLInfo;
import org.klojang.util.ModulePrivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.sql.Statement.NO_GENERATED_KEYS;
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static org.klojang.check.CommonChecks.*;
import static org.klojang.check.CommonExceptions.STATE;
import static org.klojang.convert.NumberMethods.convert;
import static org.klojang.invoke.NoSuchPropertyException.noSuchProperty;
import static org.klojang.util.ClassMethods.box;

/**
 * Facilitates the execution of SQL INSERT statements.
 */
public final class SQLInsert extends SQLStatement<SQLInsert> {

  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(SQLInsert.class);
  private static final String NO_KEYS_GENERATED = "no keys were generated";
  private static final String MULTIPLE_AUTO_KEYS = "multiple auto-increment keys not supported";
  private static final String DIRTY_INSTANCE = "insertAll() not allowed on dirty instance; call reset() first";

  private final List<String> idProperties = new ArrayList<>(5);

  private PreparedStatement ps;
  private boolean generateKeys;

  /**
   * For internal use only.
   */
  @ModulePrivate
  public SQLInsert(Connection con, AbstractSQLSession sql, SQLInfo sqlInfo) {
    super(con, sql, sqlInfo);
    this.ps = JDBC.getPreparedStatement(con, sqlInfo);
  }

  /**
   * Binds the values in the specified JavaBean to the parameters within the SQL
   * statement. Bean properties that do not correspond to named parameters will be
   * ignored. The effect of passing anything other than a proper JavaBean (e.g. an
   * {@code Integer}, {@code String} or array) is undefined.
   *
   * @param bean The bean whose values to bind to the named parameters within the SQL
   *     statement
   * @return this {@code SQLInsert} instance
   */
  @Override
  public SQLInsert bind(Object bean) {
    super.bind(bean);
    idProperties.add(null);
    return this;
  }

  /**
   * <p>Binds the values in the specified JavaBean to the parameters within the SQL
   * statement. Bean properties that do not correspond to named parameters will be
   * ignored. The effect of passing anything other than a proper JavaBean (e.g. an
   * {@code Integer}, {@code String} or array) is undefined. The {@code idProperty}
   * argument must be the name of the property corresponding to the primary key. The
   * generated value for that column will be bound back into the bean. Therefore, make
   * sure the bean is modifiable.
   *
   * <p><b><i>Klojang JDBC</i> does not support table definitions that generate keys for
   * multiple columns.</b>
   *
   * @param bean the bean whose values to bind to the named parameters within the SQL
   *     statement
   * @param idProperty the name of the property representing the auto-generated
   *     primary key.
   * @return this {@code SQLInsert} instance
   */
  public SQLInsert bind(Object bean, String idProperty) {
    super.bind(bean);
    Check.notNull(idProperty, "idProperty").then(idProperties::add);
    return this;
  }

  /**
   * Binds the values in the specified map to the parameters within the SQL statement.
   * Keys that do not correspond to named parameters will be ignored.
   *
   * @param map the map whose values to bind to the named parameters within the SQL
   *     statement
   * @return this {@code SQLInsert} instance
   */
  @Override
  public SQLInsert bind(Map<String, Object> map) {
    super.bind(map);
    idProperties.add(null);
    return this;
  }

  /**
   * Binds the values in the specified map to the parameters within the SQL statement.
   * Keys that do not correspond to named parameters will be ignored. The {@code idKey}
   * argument must be the name of the map key corresponding to the table's primary key.
   * The generated value for that column will be bound back into the map. Therefore, make
   * sure the map is modifiable.
   *
   * <p><b><i>Klojang JDBC</i> does not support table definitions that generate keys for
   * multiple columns.</b>
   *
   * @param map the map whose values to bind to the named parameters within the SQL
   *     statement
   * @param idKey the name of the map key representing the auto-generated primary
   *     key.
   * @return this {@code SQLInsert} instance
   */
  public SQLInsert bind(Map<String, ?> map, String idKey) {
    super.bind(map);
    Check.notNull(idKey, "idKey").then(idProperties::add);
    return this;
  }

  /**
   * Executes the INSERT statement. If any JavaBean was bound while specifying its ID
   * property, the key generated by the database will be bound back into the JavaBean. The
   * same applies <i>mutatis mutandis</i> for {@code Map} objects.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void execute() {
    try {
      if (containsBindBackRequest()) {
        exec(true);
        try (ResultSet rs = ps.getGeneratedKeys()) {
          Check.that(rs.next()).is(yes(), NO_KEYS_GENERATED);
          Check.that(rs.getMetaData().getColumnCount()).is(eq(), 1, MULTIPLE_AUTO_KEYS);
          long dbKey = rs.getLong(1);
          for (int i = 0; i < idProperties.size(); ++i) {
            String idProperty = idProperties.get(i);
            if (idProperty != null) {
              Object obj = bindings.get(i);
              if (obj instanceof Map map) {
                map.put(idProperty, dbKey);
              } else {
                setID(obj, idProperty, dbKey);
              }
            }
          }
        }
      } else {
        exec(false);
      }
    } catch (Throwable t) {
      throw KlojangSQLException.wrap(t, sqlInfo);
    } finally {
      reset();
    }
  }

  /**
   * Executes the INSERT statement and returns the key generated by the database. If the
   * database generated keys for multiple columns, a {@link KlojangSQLException} is
   * thrown, as this is not supported by <i>Klojang JDBC</i>.
   *
   * @return the key generated by the database
   */
  public long executeAndGetID() {
    try {
      exec(true);
      try (ResultSet rs = ps.getGeneratedKeys()) {
        Check.that(rs.next()).is(yes(),
            () -> new KlojangSQLException(NO_KEYS_GENERATED));
        Check.that(rs.getMetaData().getColumnCount()).is(eq(), 1,
            () -> new KlojangSQLException(MULTIPLE_AUTO_KEYS));
        return rs.getLong(1);
      }
    } catch (Throwable t) {
      throw KlojangSQLException.wrap(t, sqlInfo);
    } finally {
      reset();
    }
  }

  /**
   * Saves the specified JavaBeans to the database. This method combines the binding and
   * execution phase. Therefore it must be called on a "fresh" instance. That is, it must
   * not contain any bound values, beans or maps yet &#8212; which would be the case if
   * the instance has just been created, or you have just executed the INSERT statement
   * (which resets the instance), or you have explicitly called {@link #reset()}.
   *
   * @param beans the beans to save
   * @param <U> the type of the beans
   */
  public <U> void insertAll(Collection<U> beans) {
    Check.on(STATE, bindings).is(empty(), DIRTY_INSTANCE);
    try {
      for (U bean : beans) {
        bind(bean).execute();
      }
    } catch (Throwable t) {
      throw KlojangSQLException.wrap(t, sqlInfo);
    }
  }

  /**
   * Saves the specified JavaBeans to the database and sets the specified ID property in
   * each of them to the key generated by the database. This method combines the binding
   * and execution phase. Therefore it must be called on a "fresh" instance. That is, it
   * must not contain any bound values, beans or maps yet &#8212; which would be the case
   * if the instance has just been created, or you have just executed the INSERT statement
   * (which resets the instance), or you have explicitly called {@link #reset()}.
   *
   * @param beans the beans to save
   * @param idProperty the name of the property corresponding to the primary key
   * @param <U> the type of the beans
   */
  public <U> void insertAllAndSetIDs(Collection<U> beans, String idProperty) {
    Check.on(STATE, bindings).is(empty(), DIRTY_INSTANCE);
    try {
      for (U bean : beans) {
        long key = bind(bean, idProperty).executeAndGetID();
        setID(bean, idProperty, key);
      }
    } catch (Throwable t) {
      throw KlojangSQLException.wrap(t, sqlInfo);
    }
  }

  /**
   * Saves the specified JavaBeans to the database and returns the keys generated by the
   * database. This method combines the binding and execution phase. Therefore it must be
   * called on a "fresh" instance. That is, it must not contain any bound values, beans or
   * maps yet &#8212; which would be the case if the instance has just been created, or
   * you have just executed the INSERT statement (which resets the instance), or you have
   * explicitly called {@link #reset()}.
   *
   * @param beans the beans to save
   * @param idProperty the name of the property corresponding to the primary key
   * @param <U> the type of the beans
   * @return the keys generated by the database
   */
  public <U> long[] insertAllAndGetIDs(Collection<U> beans, String idProperty) {
    Check.on(STATE, bindings).is(empty(), DIRTY_INSTANCE);
    long[] keys = new long[beans.size()];
    try {
      int i = 0;
      for (U bean : beans) {
        keys[i++] = bind(bean, idProperty).executeAndGetID();
      }
    } catch (Throwable t) {
      throw KlojangSQLException.wrap(t, sqlInfo);
    }
    return keys;
  }

  @Override
  void initialize() {
    idProperties.clear();
    try {
      ps.clearParameters();
    } catch (SQLException e) {
      throw KlojangSQLException.wrap(e, sqlInfo);
    }
  }

  /**
   * Releases all resources held by this instance. You cannot reuse this instance after a
   * call to this method.
   */
  @Override
  public void close() {
    close(ps);
  }

  private void exec(boolean generateKeys) throws Throwable {
    int x = generateKeys ? RETURN_GENERATED_KEYS : NO_GENERATED_KEYS;
    if (ps == null) {
      ps = con.prepareStatement(sqlInfo.jdbcSQL(), x);
    } else if (this.generateKeys != generateKeys) {
      ps.close();
      ps = con.prepareStatement(sqlInfo.jdbcSQL(), x);
    }
    this.generateKeys = generateKeys;
    applyBindings(ps);
    ps.executeUpdate();
  }

  private boolean containsBindBackRequest() {
    return idProperties.stream().anyMatch(notNull());
  }

  private static void setID(Object bean, String idProperty, long value)
      throws Throwable {
    SetterFactory sf = SetterFactory.INSTANCE;
    Map<String, Setter> setters = sf.getSetters(bean.getClass());
    Check.that(idProperty).is(keyIn(), setters, () -> noSuchProperty(bean, idProperty));
    Setter setter = setters.get(idProperty);
    // make sure we're OK if the ID property is not a long
    Number id = convert(value, (Class) box(setter.getParamType()));
    setter.write(bean, id);
  }

}
