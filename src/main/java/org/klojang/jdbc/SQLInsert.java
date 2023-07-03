package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.invoke.Setter;
import org.klojang.invoke.SetterFactory;
import org.klojang.jdbc.x.sql.AbstractSQLSession;
import org.klojang.jdbc.x.sql.SQLInfo;
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

public final class SQLInsert extends SQLStatement<SQLInsert> {

  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(SQLInsert.class);
  private static final String NO_KEYS_GENERATED = "no keys were generated";
  private static final String MULTIPLE_AUTO_KEYS = "multiple auto-increment keys not supported";

  private final List<String> keys;

  private PreparedStatement ps;
  private boolean generateKeys;

  public SQLInsert(Connection conn, AbstractSQLSession sql, SQLInfo sqlInfo) {
    super(conn, sql, sqlInfo);
    this.keys = new ArrayList<>(5);
  }

  public SQLInsert bind(Map<String, Object> map) {
    super.bind(map);
    keys.add(null);
    return this;
  }

  public SQLInsert bind(Object bean) {
    super.bind(bean);
    keys.add(null);
    return this;
  }

  /**
   * Binds the values in the specified JavaBean to the parameters within the SQL
   * statement. Bean properties that do not correspond to named parameters will be
   * ignored. The effect of passing anything other than a proper JavaBean (e.g. an
   * {@code Integer}, {@code String} or array) is undefined. The {@code idProperty}
   * argument must be the name of the property corresponding to the auto-increment column.
   * The generated value for that column will be bound back into the bean. Therefore, make
   * sure the bean is modifiable.
   *
   * <p><b><i>Klojang JDBC</i> does not support INSERT statements that generate multiple
   * keys or non-integer keys.</b>
   *
   * @param bean the bean whose values to bind to the named parameters within the SQL
   *     statement
   * @param idProperty the name of the property representing the auto-generated
   *     primary key.
   * @return this {@code SQLInsert} instance
   */
  public SQLInsert bind(Object bean, String idProperty) {
    super.bind(bean);
    keys.add(idProperty);
    return this;
  }

  /**
   * Binds the values in the specified map to the parameters within the SQL statement.
   * Keys that do not correspond to named parameters will be ignored. The {@code idKey}
   * argument must be the name of the property corresponding to the auto-increment column.
   * The generated value for that column will be bound back into the map using
   * {@code idKey} as the map key. Therefore, make sure the map is modifiable.
   *
   * <p><b><i>Klojang JDBC</i> does not support INSERT statements that generate multiple
   * keys or non-integer keys.</b>
   *
   * @param map the bean whose values to bind to the named parameters within the SQL
   *     statement
   * @param idKey the name of the map key representing the auto-generated primary
   *     key.
   * @return this {@code SQLInsert} instance
   */
  public SQLInsert bind(Map<String, ?> map, String idKey) {
    super.bind(map);
    keys.add(idKey);
    return this;
  }

  public <U> void insertAll(Collection<U> beans) {
    Check.on(STATE, bindables).is(empty(), "insertAll() not allowed on dirty instance");
    try {
      for (U bean : beans) {
        bindables.clear();
        bindables.add(bean);
        exec(false);
      }
    } catch (Throwable t) {
      throw KlojangSQLException.wrap(t, sqlInfo);
    } finally {
      reset();
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void execute() {
    boolean mustBindBack = keys.stream().anyMatch(notNull());
    try {
      if (!mustBindBack) {
        exec(false);
      } else {
        exec(true);
        try (ResultSet rs = ps.getGeneratedKeys()) {
          Check.that(rs.next()).is(yes(), NO_KEYS_GENERATED);
          Check.that(rs.getMetaData().getColumnCount()).is(ne(), 1, MULTIPLE_AUTO_KEYS);
          long id = rs.getLong(1);
          for (int i = 0; i < keys.size(); ++i) {
            String key = keys.get(i);
            if (key != null) {
              Object obj = bindables.get(i);
              if (obj instanceof Map) {
                ((Map) obj).put(key, id);
              } else {
                Map<String, Setter> setters = SetterFactory.INSTANCE.getSetters(
                    obj.getClass());
                Check.on(s -> noSuchProperty(obj, key), key).is(keyIn(), setters);
                Setter setter = setters.get(key);
                Number n = convert(
                    id,
                    (Class<? extends Number>) box(setter.getParamType()));
                setter.write(obj, n);
              }
            }
          }
        }
      }
    } catch (Throwable t) {
      throw KlojangSQLException.wrap(t, sqlInfo);
    } finally {
      reset();
    }
  }

  public long executeAndGetID() {
    try {
      try {
        exec(true);
      } catch (Throwable t) {
        throw KlojangSQLException.wrap(t, sqlInfo);
      }
      try (ResultSet rs = ps.getGeneratedKeys()) {
        Check.that(rs.next()).is(yes(), NO_KEYS_GENERATED);
        Check.that(rs.getMetaData().getColumnCount()).is(ne(), 1, MULTIPLE_AUTO_KEYS);
        return rs.getLong(1);
      }
    } catch (SQLException e) {
      throw KlojangSQLException.wrap(e, sqlInfo);
    } finally {
      reset();
    }
  }

  @Override
  public void close() {
    close(ps);
  }

  private void exec(boolean generateKeys) throws Throwable {
    if (ps == null) {
      this.generateKeys = generateKeys;
      int i = generateKeys ? RETURN_GENERATED_KEYS : NO_GENERATED_KEYS;
      ps = con.prepareStatement(sqlInfo.jdbcSQL(), i);
    } else if (this.generateKeys != generateKeys) {
      this.generateKeys = generateKeys;
      int i = generateKeys ? RETURN_GENERATED_KEYS : NO_GENERATED_KEYS;
      ps.close();
      ps = con.prepareStatement(sqlInfo.jdbcSQL(), i);
    }
    applyBindings(ps);
    ps.executeUpdate();
  }

  private void reset() {
    bindables.clear();
    keys.clear();
    try {
      ps.clearParameters();
    } catch (SQLException e) {
      throw KlojangSQLException.wrap(e, sqlInfo);
    }
  }
}
