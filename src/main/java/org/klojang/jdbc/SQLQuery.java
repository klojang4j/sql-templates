package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.rs.ColumnReader;
import org.klojang.jdbc.x.rs.ColumnReaderFinder;
import org.klojang.jdbc.x.sql.AbstractSQLSession;
import org.klojang.jdbc.x.sql.SQLInfo;
import org.klojang.templates.NameMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static org.klojang.check.CommonChecks.yes;

public final class SQLQuery extends SQLStatement<SQLQuery> {

  private static final Logger LOG = LoggerFactory.getLogger(SQLQuery.class);

  private NameMapper mapper = NameMapper.AS_IS;

  private PreparedStatement ps;
  private ResultSet rs;

  public SQLQuery(Connection con, AbstractSQLSession sql, SQLInfo sqlInfo) {
    super(con, sql, sqlInfo);
  }

  /**
   * Sets the {@code NameMapper} to be used when mapping column names to be bean
   * properties or map keys. Beware of the direction of the mappings: <i>from</i> column
   * names <i>to</i> bean properties (or map keys).
   *
   * @param columnMapper the {@code NameMapper} to be used when mapping column names
   *     to bean properties or map keys.
   * @return This {@code SQLQuery} instance
   */
  public SQLQuery withMapper(NameMapper columnMapper) {
    this.mapper = Check.notNull(columnMapper).ok();
    return this;
  }

  /**
   * Executes the query and returns the {@link ResultSet} produced by the JDBC driver. If
   * the query had already been executed, the initial {@link ResultSet} is returned.
   *
   * @return The {@code ResultSet} produced by the JDBC driver
   */
  public ResultSet getResultSet() {
    try {
      return rs();
    } catch (Throwable t) {
      throw KlojangSQLException.wrap(t, sqlInfo);
    }
  }

  /**
   * Executes the query and returns the value of the first column in the first row. If the
   * query had already been executed, you get the value from the second row, etc. Throws a
   * {@link KlojangSQLException} if the query returned zero rows or if there are no more
   * rows in the {@code ResultSet}.
   *
   * @param <T> The type of the value to be returned
   * @param clazz the class of the value to be returned
   * @return The value of the first column in the first row
   * @throws KlojangSQLException If the query returned zero rows
   */
  public <T> T lookup(Class<T> clazz) {
    ResultSet rs = executeAndNext();
    try {
      int sqlType = rs.getMetaData().getColumnType(1);
      ColumnReader<?, T> emitter = ColumnReaderFinder.getInstance().findReader(
          clazz,
          sqlType);
      return emitter.getValue(rs, 1, clazz);
    } catch (Throwable t) {
      throw KlojangSQLException.wrap(t, sqlInfo);
    }
  }

  /**
   * Executes the query and returns the value of the first column in the first row as an
   * integer. If the query had already been executed, you get the value from the second
   * row, etc. Throws a {@link KlojangSQLException} if the query returned zero rows or if
   * there are no more rows in the {@code ResultSet}.
   *
   * @return The value of the first column in the first row as an integer
   * @throws KlojangSQLException If the query returned zero rows
   */
  public int getInt() throws KlojangSQLException {
    try {
      return executeAndNext().getInt(1);
    } catch (SQLException e) {
      throw KlojangSQLException.wrap(e, sqlInfo);
    }
  }

  /**
   * Returns the value of the first column of the first row as a {@code String}. If the
   * query had already been executed, you get the value from the second row, etc. Throws a
   * {@link KlojangSQLException} if the query returned zero rows or if there are no more
   * rows in the {@code ResultSet}.
   *
   * @return The value of the first column of the first row as aa {@code String}
   * @throws KlojangSQLException If the query returned zero rows
   */
  public String getString() throws KlojangSQLException {
    try {
      return executeAndNext().getString(1);
    } catch (SQLException e) {
      throw KlojangSQLException.wrap(e, sqlInfo);
    }
  }

  /**
   * Returns a {@code List} of the all values in the first column. Equivalent to
   * {@code getList(clazz, 10)}.
   *
   * @param <T> The desired type of the values
   * @param clazz the desired class of the values
   * @return A {@code List} of the values of the first column in the rows selected by the
   *     query
   */
  public <T> List<T> getList(Class<T> clazz) {
    return getList(clazz, 10);
  }

  /**
   * Returns a {@code List} of the all values in the first column. In other words, this
   * method will exhaust the {@link ResultSet}.
   *
   * @param <T> the desired type of the values
   * @param clazz the desired class of the values
   * @param expectedSize the expected number of rows
   * @return A {@code List} of the values of the first column in the rows selected by the
   *     query
   */
  public <T> List<T> getList(Class<T> clazz, int expectedSize) {
    try {
      ResultSet rs = rs();
      if (!rs.next()) {
        return Collections.emptyList();
      }
      int sqlType = rs.getMetaData().getColumnType(1);
      ColumnReader<?, T> extractor = ColumnReaderFinder.getInstance().findReader(
          clazz,
          sqlType);
      List<T> list = new ArrayList<>(expectedSize);
      do {
        list.add(extractor.getValue(rs, 1, clazz));
      } while (rs.next());
      return list;
    } catch (Throwable t) {
      throw KlojangSQLException.wrap(t, sqlInfo);
    }
  }

  /**
   * Executes the query and returns a {@code ResultSetMappifier} that you can use to
   * convert the rows in the {@link ResultSet} into {@code Map<String, Object>}
   * instances.
   *
   * @return A {@code ResultSetMappifier} that you can use to convert the rows in the
   *     {@link ResultSet} into {@code Map<String, Object>} instances.
   */
  public ResultSetMappifier getMappifier() {
    try {
      return session.getSQL().getMappifierFactory(mapper).getResultSetMappifier(
          rs());
    } catch (Throwable t) {
      throw KlojangSQLException.wrap(t, sqlInfo);
    }
  }

  /**
   * Executes the query and returns a {@code ResultSetBeanifier} that you can use to
   * convert the rows in the {@link ResultSet} into JavaBeans.
   *
   * @param <T> The type of the JavaBeans
   * @param beanClass the class of the JavaBeans
   * @return A {@code ResultSetBeanifier} that you can use to convert the rows in the
   *     {@link ResultSet} into JavaBeans.
   */
  public <T> ResultSetBeanifier<T> getBeanifier(Class<T> beanClass) {
    try {
      return session.getSQL().getBeanifierFactory(beanClass, mapper).getBeanifier(
          rs());
    } catch (Throwable t) {
      throw KlojangSQLException.wrap(t, sqlInfo);
    }
  }

  /**
   * Executes the query and returns a {@code ResultSetBeanifier} that you can use to
   * convert the rows in the {@link ResultSet} into JavaBeans.
   *
   * @param <T> The type of the JavaBeans
   * @param beanClass the class of the JavaBeans
   * @param beanSupplier the supplier of the JavaBean instances
   * @return A {@code ResultSetBeanifier} that you can use to convert the rows in the
   *     {@link ResultSet} into JavaBeans.
   */
  public <T> ResultSetBeanifier<T> getBeanifier(Class<T> beanClass,
                                                Supplier<T> beanSupplier) {
    try {
      return session.getSQL()
          .getBeanifierFactory(beanClass, beanSupplier, mapper)
          .getBeanifier(rs());
    } catch (Throwable t) {
      throw KlojangSQLException.wrap(t, sqlInfo);
    }
  }

  @Override
  public void close() {
    close(ps);
  }

  private ResultSet executeAndNext() {
    ResultSet rs;
    boolean hasRows;
    try {
      hasRows = (rs = rs()).next();
    } catch (Throwable t) {
      throw KlojangSQLException.wrap(t, sqlInfo);
    }
    Check.on(KlojangSQLException::new, hasRows).is(yes(), "query returned zero rows");
    return rs;
  }

  private ResultSet rs() throws Throwable {
    if (rs == null) {
      LOG.trace("Executing query");
      rs = ps().executeQuery();
    }
    return rs;
  }

  private PreparedStatement ps() throws Throwable {
    if (ps == null) {
      ps = con.prepareStatement(sqlInfo.jdbcSQL());
      applyBindings(ps);
    }
    return ps;
  }
}
