package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.JDBC;
import org.klojang.jdbc.x.sql.AbstractSQLSession;
import org.klojang.jdbc.x.sql.SQLInfo;
import org.klojang.util.ModulePrivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.sql.Statement.NO_GENERATED_KEYS;
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static org.klojang.check.CommonChecks.empty;
import static org.klojang.check.CommonExceptions.illegalState;

/**
 * Facilitates the execution of SQL INSERT statements.
 */
public final class SQLInsert extends SQLStatement<SQLInsert> {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(SQLInsert.class);
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
    }

    /**
     * Binds the values in the specified JavaBean to the parameters within the SQL
     * statement. Bean properties that do not correspond to named parameters will be
     * ignored. The effect of passing anything other than a proper JavaBean (e.g. an
     * {@code Integer}, {@code String} or array) is undefined.
     *
     * @param bean The bean whose values to bind to the named parameters within the SQL
     * statement
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
     * <p><b><i>Klojang JDBC</i> does not support table definitions that generate keys
     * for multiple columns.</b>
     *
     * @param bean the bean whose values to bind to the named parameters within the SQL
     * statement
     * @param idProperty the name of the property representing the auto-generated primary
     * key.
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
     * statement
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
     * The generated value for that column will be bound back into the map. Therefore,
     * make sure the map is modifiable.
     *
     * <p><b><i>Klojang JDBC</i> does not support table definitions that generate keys
     * for multiple columns.</b>
     *
     * @param map the map whose values to bind to the named parameters within the SQL
     * statement
     * @param idKey the name of the map key representing the auto-generated primary key.
     * @return this {@code SQLInsert} instance
     */
    public SQLInsert bind(Map<String, ?> map, String idKey) {
        super.bind(map);
        Check.notNull(idKey, "idKey").then(idProperties::add);
        return this;
    }

    /**
     * Executes the INSERT statement. Auto-generated keys will not be passed back to the
     * client.
     */
    public void execute() {
        try {
            exec(false);
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
            return JDBC.getGeneratedKeys(ps, 1)[0];
        } catch (Throwable t) {
            throw KlojangSQLException.wrap(t, sqlInfo);
        } finally {
            reset();
        }
    }

    /**
     * Executes the INSERT statement and returns the key generated by the database. Any
     * JavaBean that was bound using {@link #bind(Object, String) bind(bean, idProperty)}
     * will have its ID property set to the key generated by the database. JavaBeans that
     * were bound using {@link #bind(Object) bind(bean)} will remain unmodified. The same
     * applies <i>mutatis mutandis</i> for {@code Map} objects.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void executeAndSetID() {
        try {
            exec(true);
            long dbKey = JDBC.getGeneratedKeys(ps, 1)[0];
            for (int i = 0; i < idProperties.size(); ++i) {
                String idProperty = idProperties.get(i);
                if (idProperty != null) {
                    Object obj = bindings.get(i);
                    if (obj instanceof Map map) {
                        map.put(idProperty, dbKey);
                    } else {
                        JDBC.setID(obj, idProperty, dbKey);
                    }
                }
            }
        } catch (Throwable t) {
            throw KlojangSQLException.wrap(t, sqlInfo);
        } finally {
            reset();
        }
    }

    /**
     * Saves the specified JavaBeans to the database. This method combines the binding and
     * execution phase. Therefore it must be called on a "fresh" instance. That is, it
     * must not contain any bound values, beans or maps yet &#8212; which would be the
     * case if the instance has just been created, or you have just executed the INSERT
     * statement (which resets the instance), or you have explicitly called
     * {@link #reset()}.
     *
     * <p>For large batches consider using the {@link SQLBatchInsert} class as it is
     * likely to be more performant.
     *
     * @param beans the beans to save
     * @param <U> the type of the beans
     */
    public <U> void insertBatch(Collection<U> beans) {
        Check.that(bindings).is(empty(), illegalState(DIRTY_INSTANCE));
        try {
            for (U bean : beans) {
                bind(bean).exec(false);
                reset();
            }
        } catch (Throwable t) {
            throw KlojangSQLException.wrap(t, sqlInfo);
        }
    }

    /**
     * Saves the specified JavaBeans to the database and returns the keys generated by the
     * database. This method combines the binding and execution phase. Therefore it must
     * be called on a "fresh" instance. That is, it must not contain any bound values,
     * beans or maps yet &#8212;. This will be the case if the instance has just been
     * created or if you just executed the INSERT statement (which resets the instance) or
     * if you just called {@link #reset()}.
     *
     * <p>For large batches consider using the {@link SQLBatchInsert} class as it is
     * likely to be more performant.
     *
     * @param beans the beans to save
     * @param <U> the type of the beans
     * @return the keys generated by the database
     */
    public <U> long[] insertBatchAndGetIDs(Collection<U> beans) {
        Check.that(bindings).is(empty(), illegalState(DIRTY_INSTANCE));
        long[] keys = new long[beans.size()];
        try {
            int i = 0;
            for (U bean : beans) {
                bind(bean).exec(true);
                keys[i++] = JDBC.getGeneratedKeys(ps, 1)[0];
                reset();
            }
        } catch (Throwable t) {
            throw KlojangSQLException.wrap(t, sqlInfo);
        }
        return keys;
    }

    /**
     * Saves the specified JavaBeans to the database and sets the specified ID property in
     * each of them to the key generated by the database. This method combines the binding
     * and execution phase. Therefore it must be called on a "fresh" instance. That is, it
     * must not contain any bound values, beans or maps yet &#8212; which would be the
     * case if the instance has just been created, or you have just executed the INSERT
     * statement (which resets the instance), or you have explicitly called
     * {@link #reset()}.
     *
     * @param <U> the type of the beans
     * @param idProperty the name of the property corresponding to the primary key
     * @param beans the beans to save
     */
    public <U> void insertBatchAndSetIDs(String idProperty, Collection<U> beans) {
        Check.that(bindings).is(empty(), illegalState(DIRTY_INSTANCE));
        try {
            for (U bean : beans) {
                bind(bean, idProperty).exec(true);
                long key = JDBC.getGeneratedKeys(ps, 1)[0];
                JDBC.setID(bean, idProperty, key);
                reset();
            }
        } catch (Throwable t) {
            throw KlojangSQLException.wrap(t, sqlInfo);
        }
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
     * Releases all resources held by this instance. You cannot reuse this instance after
     * a call to this method.
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

}
