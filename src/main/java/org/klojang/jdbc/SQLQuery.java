package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.JDBC;
import org.klojang.jdbc.x.rs.ColumnReader;
import org.klojang.jdbc.x.rs.ColumnReaderFinder;
import org.klojang.jdbc.x.sql.AbstractSQLSession;
import org.klojang.jdbc.x.sql.SQLInfo;
import org.klojang.templates.NameMapper;
import org.klojang.util.ModulePrivate;
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

/**
 * <p>Facilitates the execution of SQL SELECT statements. {@code SQLQuery} instances are
 * obtained via {@link SQLSession#prepareQuery(Connection) SQLSession.prepareQuery()}. You
 * should always obtain them using a try-with-resources block. Here is a simple example of
 * how you can use the {@code SQLQuery} class:
 *
 * <blockquote><pre>{@code
 * SQL sql = SQL.basic("SELECT * FROM PERSON WHERE FIRST_NAME = :firstName");
 * try(SQLQuery query = sql.session().prepareQuery(jdbcConnection)) {
 *  query.bind("firstName", "John");
 *  List<Person> persons = query.getBeanifier(Person.class).beanifyAll();
 * }
 * }</pre></blockquote>
 *
 * <p>Here is an example of SQL that contains both named parameters and template
 * variables (see {@link SQL} for more information):
 *
 * <blockquote><pre>{@code
 * String queryString = """
 *  SELECT LAST_NAME
 *    FROM PERSON
 *   WHERE FIRST_NAME = :firstName
 *   ORDER BY :sortColumn
 *  """;
 * SQL sql = SQL.template(queryString);
 * SQLSession session = sql.session();
 * session.setSortColumn("BIRTH_DATE");
 * try(SQLQuery query = session.prepareQuery(jdbcConnection)) {
 *  query.bind("firstName", "John");
 *  List<String> lastNames = query.firstColumn();
 * }
 * }</pre></blockquote>
 */
public final class SQLQuery extends SQLStatement<SQLQuery> {

  private static final Logger LOG = LoggerFactory.getLogger(SQLQuery.class);

  private final PreparedStatement ps;

  private NameMapper mapper = NameMapper.AS_IS;
  private ResultSet resultSet;

  /**
   * For internal use only.
   */
  @ModulePrivate
  public SQLQuery(Connection con, AbstractSQLSession sql, SQLInfo sqlInfo) {
    super(con, sql, sqlInfo);
    this.ps = JDBC.getPreparedStatement(con, sqlInfo);
  }

  /**
   * Sets the column-to-property mapper to be used when populating JavaBeans or maps from
   * a {@link ResultSet}. Beware of the direction of the mappings: <i>from</i> column
   * names <i>to</i> bean properties (or map keys).
   *
   * @param columnMapper the column-to-property mapper to be used
   * @return this {@code SQLQuery} instance
   */
  public SQLQuery withMapper(NameMapper columnMapper) {
    this.mapper = Check.notNull(columnMapper).ok();
    return this;
  }

  /**
   * Executes the query and returns the {@link ResultSet}. If the query had already been
   * executed, the initial {@link ResultSet} is returned. It will not be re-executed.
   *
   * @return the {@code ResultSet} produced by the JDBC driver
   */
  public ResultSet getResultSet() {
    try {
      executeQuery();
      return resultSet;
    } catch (Throwable t) {
      throw KlojangSQLException.wrap(t, sqlInfo);
    }
  }

  /**
   * Executes the query and returns the value of the first column in the first row. The
   * second time you call this method, you get the value of the first column in the second
   * row, and so on. A {@link KlojangSQLException} is thrown if the query returned zero
   * rows or if there are no more rows in the {@code ResultSet}.
   *
   * @param <T> the type of the value to be returned
   * @param clazz the class of the value to be returned
   * @return the value of the first column in the first row
   * @throws KlojangSQLException if the query returned zero rows
   */
  public <T> T lookup(Class<T> clazz) {
    try {
      executeAndNext();
      int sqlType = resultSet.getMetaData().getColumnType(1);
      return ColumnReaderFinder
          .getInstance()
          .findReader(clazz, sqlType)
          .getValue(resultSet, 1, clazz);
    } catch (Throwable t) {
      throw KlojangSQLException.wrap(t, sqlInfo);
    }
  }

  /**
   * Executes the query and returns the value of the first column in the first row as an
   * {@code int}. The second time you call this method, you get the value of the first
   * column in the second row, and so on. A {@link KlojangSQLException} is thrown if the
   * query returned zero rows or if there are no more rows in the {@code ResultSet}.
   *
   * @return the value of the first column in the first row as an integer
   * @throws KlojangSQLException if the query returned zero rows
   */
  public int getInt() throws KlojangSQLException {
    try {
      executeAndNext();
      return resultSet.getInt(1);
    } catch (Throwable t) {
      throw KlojangSQLException.wrap(t, sqlInfo);
    }
  }

  /**
   * Executes the query and returns the value of the first column of the first row as a
   * {@code String}. The second time you call this method, you get the value of the first
   * column in the second row, and so on. A {@link KlojangSQLException} is thrown if the
   * query returned zero rows or if there are no more rows in the {@code ResultSet}.
   *
   * @return the value of the first column of the first row as aa {@code String}
   * @throws KlojangSQLException If the query returned zero rows
   */
  public String getString() throws KlojangSQLException {
    try {
      executeAndNext();
      return resultSet.getString(1);
    } catch (Throwable t) {
      throw KlojangSQLException.wrap(t, sqlInfo);
    }
  }

  /**
   * Executes the query and returns a {@code List} of all values in the first column of
   * the result set. Equivalent to {@link #firstColumn(Class) firstColumn(String.class)}.
   *
   * @return the values of the first column in the result set
   */
  public List<String> firstColumn() { return firstColumn(String.class); }

  /**
   * Executes the query and returns a {@code List} of all values in the first column of
   * the result set. Equivalent to
   * {@link #firstColumn(Class, int) firstColumn(clazz, 10)}.
   *
   * @param <T> the desired type of the values
   * @param clazz the desired class of the values
   * @return the values of the first column in the result set
   */
  public <T> List<T> firstColumn(Class<T> clazz) { return firstColumn(clazz, 10); }

  /**
   * Executes the query and returns a {@code List} of the all values in the first column.
   * In other words, this method will exhaust the {@link ResultSet}.
   *
   * @param <T> the desired type of the values
   * @param clazz the desired class of the values
   * @param sizeEstimate the expected number of rows in the result set
   * @return the values of the first column in the result set
   */
  public <T> List<T> firstColumn(Class<T> clazz, int sizeEstimate) {
    try {
      executeQuery();
      if (!resultSet.next()) {
        return Collections.emptyList();
      }
      int sqlType = resultSet.getMetaData().getColumnType(1);
      ColumnReader<?, T> reader = ColumnReaderFinder.getInstance()
          .findReader(clazz, sqlType);
      List<T> list = new ArrayList<>(sizeEstimate);
      do {
        list.add(reader.getValue(resultSet, 1, clazz));
      } while (resultSet.next());
      return list;
    } catch (Throwable t) {
      throw KlojangSQLException.wrap(t, sqlInfo);
    }
  }

  /**
   * Executes the query and returns a {@code ResultSetMappifier} that you can use to
   * convert the rows in the {@link ResultSet} into {@code Map<String, Object>} pseudo
   * objects.
   *
   * @return a {@code ResultSetMappifier} that you can use to convert the rows in the
   *     {@link ResultSet} into {@code Map<String, Object>} pseudo objects.
   */
  public ResultSetMappifier getMappifier() {
    try {
      executeQuery();
      return session
          .getSQL()
          .getMappifierFactory(mapper)
          .getResultSetMappifier(resultSet);
    } catch (Throwable t) {
      throw KlojangSQLException.wrap(t, sqlInfo);
    }
  }

  /**
   * Executes the query and returns a {@code ResultSetBeanifier} that you can use to
   * convert the rows in the {@link ResultSet} into JavaBeans.
   *
   * @param <T> the type of the JavaBeans
   * @param beanClass the class of the JavaBeans
   * @return a {@code ResultSetBeanifier} that you can use to convert the rows in the
   *     {@link ResultSet} into JavaBeans.
   */
  public <T> ResultSetBeanifier<T> getBeanifier(Class<T> beanClass) {
    try {
      executeQuery();
      return session
          .getSQL()
          .getBeanifierFactory(beanClass, mapper)
          .getBeanifier(resultSet);
    } catch (Throwable t) {
      throw KlojangSQLException.wrap(t, sqlInfo);
    }
  }

  /**
   * Executes the query and returns a {@code ResultSetBeanifier} that you can use to
   * convert the rows in the {@link ResultSet} into JavaBeans.
   *
   * @param <T> the type of the JavaBeans
   * @param beanClass the class of the JavaBeans
   * @param beanSupplier the supplier of the JavaBean instances. This would ordinarily
   *     be a method reference to the constructor of the JavaBean (like
   *     {@code Person::new})
   * @return a {@code ResultSetBeanifier} that you can use to convert the rows in the
   *     {@link ResultSet} into JavaBeans.
   */
  public <T> ResultSetBeanifier<T> getBeanifier(
      Class<T> beanClass,
      Supplier<T> beanSupplier) {
    try {
      executeQuery();
      return session
          .getSQL()
          .getBeanifierFactory(beanClass, beanSupplier, mapper)
          .getBeanifier(resultSet);
    } catch (Throwable t) {
      throw KlojangSQLException.wrap(t, sqlInfo);
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

  @Override
  void initialize() {
    try {
      if (resultSet != null) {
        resultSet.close();
        resultSet = null;
      }
      ps.clearParameters();
    } catch (SQLException e) {
      throw new KlojangSQLException(e);
    }
  }

  private void executeAndNext() throws Throwable {
    executeQuery();
    if (!resultSet.next()) {
      throw new KlojangSQLException("query returned zero rows");
    }
  }

  private void executeQuery() throws Throwable {
    if (resultSet == null) {
      LOG.trace("Executing query");
      applyBindings(ps);
      resultSet = ps.executeQuery();
    }
  }

}
