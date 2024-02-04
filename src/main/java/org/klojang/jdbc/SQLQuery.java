package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.check.aux.Result;
import org.klojang.jdbc.x.Utils;
import org.klojang.jdbc.x.rs.ColumnReader;
import org.klojang.jdbc.x.rs.ColumnReaderFactory;
import org.klojang.jdbc.x.sql.SQLInfo;
import org.klojang.templates.NameMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Supplier;

import static org.klojang.jdbc.x.Strings.EXECUTING_SQL;

/**
 * <p>Facilitates the execution of SQL SELECT statements. {@code SQLQuery} instances are
 * obtained via {@link SQLSession#prepareQuery() SQLSession.prepareQuery()}. Here is an
 * example of how to use the {@code SQLQuery} class:
 *
 * <blockquote><pre>{@code
 * SQL sql = SQL.simple("SELECT LAST_NAME FROM PERSON WHERE FIRST_NAME = :firstName");
 * try(Connection con = ...) {
 *   try(SQLQuery query = sql.session(con).prepareQuery()) {
 *     query.bind("firstName", "John");
 *     List<String> lastNames = query.firstColumn();
 *   }
 * }
 * }</pre></blockquote>
 *
 * <p>Here is an example of a query that that contains both named parameters and template
 * variables (see the {@linkplain SQL SQL interface} for more information):
 *
 * <blockquote><pre>{@code
 * String query = """
 *  SELECT LAST_NAME
 *    FROM PERSON
 *   WHERE FIRST_NAME = :firstName
 *   ORDER BY ~%sortColumn%
 *  """;
 * SQL sql = SQL.template(query);
 * try(Connection con = ...) {
 *   SQLSession session = sql.session(con);
 *   session.setIdentifier("sortColumn", "SALARY");
 *   try(SQLQuery query = session.prepareQuery()) {
 *     query.bind("firstName", "John");
 *     List<String> lastNames = query.firstColumn();
 *   }
 * }
 * }</pre></blockquote>
 */
public final class SQLQuery extends SQLStatement<SQLQuery> {

  private static final Logger LOG = LoggerFactory.getLogger(SQLQuery.class);

  private NameMapper mapper = NameMapper.AS_IS;
  private ResultSet resultSet;

  SQLQuery(PreparedStatement ps, AbstractSQLSession sql, SQLInfo sqlInfo) {
    super(ps, sql, sqlInfo);
  }

  /**
   * Sets the column-to-property mapper to be used when populating JavaBeans or maps from
   * a {@link ResultSet}. Beware of the direction of the mappings: <i>from</i> column
   * names <i>to</i> bean properties (or record components, or map keys).
   *
   * @param columnToPropertyMapper the column-to-property mapper to be used
   * @return this {@code SQLQuery} instance
   */
  public SQLQuery withNameMapper(NameMapper columnToPropertyMapper) {
    this.mapper = Check.notNull(columnToPropertyMapper).ok();
    return this;
  }

  /**
   * Executes the query and returns the raw JDBC {@link ResultSet}. If the query had
   * already been executed, it will not be re-executed. Call {@link SQLStatement#reset()}
   * to force the query to be re-executed.
   *
   * @return the {@code ResultSet} produced by the JDBC driver
   */
  public ResultSet getResultSet() {
    try {
      execute();
      return resultSet;
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  /**
   * Returns the value of the first column in the first row. The second time you call this
   * method, you get the value of the first column in the second row, and so on. If there
   * are no (more) rows in the {@code ResultSet},
   * {@link Result#notAvailable() Result.notAvailable()} is returned.
   *
   * @param <T> the type of the value to be returned
   * @param clazz the class of the value to be returned
   * @return the value of the first column in the first row or
   *       {@link Result#notAvailable() Result.notAvailable()} if there are no (more) rows
   *       in the {@code ResultSet}
   */
  public <T> Result<T> lookup(Class<T> clazz) {
    try {
      execute();
      if (resultSet.next()) {
        int sqlType = resultSet.getMetaData().getColumnType(1);
        T val = ColumnReaderFactory
              .getInstance()
              .getReader(clazz, sqlType)
              .getValue(resultSet, 1, clazz);
        return Result.of(val);
      }
      return Result.notAvailable();
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  /**
   * Executes the query and returns the value of the first column in the first row as an
   * {@code Integer}. Equivalent to {@code lookup(Integer.class}). The second time you
   * call this method, you get the value of the first column in the second row, and so on.
   * If there are no (more) rows in the {@code ResultSet}, {@link OptionalInt#empty()} is
   * returned.
   *
   * @return the value of the first column in the first row as an integer
   */
  public Result<Integer> getInt() {
    try {
      execute();
      if (resultSet.next()) {
        return Result.of(resultSet.getInt(1));
      }
      return Result.notAvailable();
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  /**
   * Executes the query and returns the value of the first column of the first row as a
   * {@code String}. Equivalent to {@code lookup(String.class}). The second time you call
   * this method, you get the value of the first column in the second row, and so on. If
   * there are no (more) rows in the {@code ResultSet}, {@link Result#notAvailable()} is
   * returned.
   *
   * @return the value of the first column of the first row as a {@code String}
   */
  public Result<String> getString() {
    try {
      execute();
      if (resultSet.next()) {
        return Result.of(resultSet.getString(1));
      }
      return Result.notAvailable();
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  /**
   * Returns {@code true} if the query yielded at least one row; {@code false} otherwise
   *
   * @return {@code true} if the query yielded at least one row; {@code false} otherwise
   */
  public boolean exists() {
    try {
      execute();
      return resultSet.next();
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  /**
   * Returns all values in the first column of the SELECT clause. Equivalent to
   * {@link #firstColumn(Class) firstColumn(String.class)}.
   *
   * @return the values of the first column in the result set
   */
  public List<String> firstColumn() { return firstColumn(String.class); }

  /**
   * Returns all values in the first column of the SELECT clause. Equivalent to
   * {@link #firstColumn(Class, int) firstColumn(clazz, 10)}.
   *
   * @param <T> the desired type of the values
   * @param clazz the desired class of the values
   * @return the values of the first column in the result set
   */
  public <T> List<T> firstColumn(Class<T> clazz) { return firstColumn(clazz, 10); }

  /**
   * Returns all values in the first column of the SELECT clause. In other words, this
   * method will retrieve all rows satisfying the WHERE and LIMIT clauses (if any) and
   * collect the values of the first column into a {@code List}.
   *
   * @param <T> the desired type of the values
   * @param clazz the desired class of the values
   * @param sizeEstimate the expected number of rows
   * @return the values of the first column in the result set
   */
  public <T> List<T> firstColumn(Class<T> clazz, int sizeEstimate) {
    try {
      execute();
      if (!resultSet.next()) {
        return Collections.emptyList();
      }
      int sqlType = resultSet.getMetaData().getColumnType(1);
      ColumnReader<?, T> reader = ColumnReaderFactory
            .getInstance()
            .getReader(clazz, sqlType);
      List<T> list = new ArrayList<>(sizeEstimate);
      do {
        list.add(reader.getValue(resultSet, 1, clazz));
      } while (resultSet.next());
      return list;
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  /**
   * Executes the query and returns a {@code ResultSetMappifier} that you can use to
   * convert the rows in the {@link ResultSet} into {@code Map<String, Object>}
   * pseudo-objects.
   *
   * @return a {@code ResultSetMappifier} that you can use to convert the rows in the
   *       {@link ResultSet} into {@code Map<String, Object>} pseudo objects.
   */
  public ResultSetMappifier getMappifier() {
    try {
      execute();
      return session
            .getSQL()
            .getMappifierFactory(mapper)
            .getMappifier(resultSet);
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  /**
   * Executes the query and returns a {@code ResultSetBeanifier} that you can use to
   * convert the rows in the {@link ResultSet} into JavaBeans.
   *
   * @param <T> the type of the JavaBeans (may be a {@code record} type)
   * @param beanClass the class of the JavaBeans
   * @return a {@code ResultSetBeanifier} that you can use to convert the rows in the
   *       {@link ResultSet} into JavaBeans.
   */
  public <T> ResultSetBeanifier<T> getBeanifier(Class<T> beanClass) {
    try {
      execute();
      return session
            .getSQL()
            .getBeanifierFactory(beanClass, mapper)
            .getBeanifier(resultSet);
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  /**
   * Executes the query and returns a {@code ResultSetBeanifier} that you can use to
   * convert the rows in the {@link ResultSet} into JavaBeans.
   *
   * @param <T> the type of the JavaBeans (may be a {@code record} type)
   * @param beanClass the class of the JavaBeans
   * @param beanSupplier the supplier of the JavaBean instances. This would
   *       ordinarily be a method reference to the constructor of the JavaBean (like
   *       {@code Person::new})
   * @return a {@code ResultSetBeanifier} that you can use to convert the rows in the
   *       {@link ResultSet} into JavaBeans.
   */
  public <T> ResultSetBeanifier<T> getBeanifier(
        Class<T> beanClass,
        Supplier<T> beanSupplier) {
    try {
      execute();
      return session
            .getSQL()
            .getBeanifierFactory(beanClass, beanSupplier, mapper)
            .getBeanifier(resultSet);
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
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
      throw Utils.wrap(e);
    }
  }

  private void execute() throws Throwable {
    if (resultSet == null) {
      LOG.trace(EXECUTING_SQL, sqlInfo.sql());
      applyBindings(ps);
      resultSet = ps.executeQuery();
    }
  }

}
