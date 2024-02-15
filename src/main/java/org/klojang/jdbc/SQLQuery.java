package org.klojang.jdbc;

import org.klojang.check.aux.Result;
import org.klojang.jdbc.x.Msg;
import org.klojang.jdbc.x.Utils;
import org.klojang.jdbc.x.rs.ColumnReader;
import org.klojang.jdbc.x.rs.ColumnReaderFactory;
import org.klojang.jdbc.x.sql.SQLInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

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
 *     // Collect all values in the first column of the ResultSet into a List
 *     List<String> lastNames = query.firstColumn();
 *   }
 * }
 * }</pre></blockquote>
 *
 * <p>Here is an example of a query that that contains both named parameters and template
 * variables (see the {@linkplain SQL comments for the SQL interface} for the difference
 * between the two):
 *
 * <blockquote><pre>{@code
 * SQL sql = SQL.template("""
 *  SELECT LAST_NAME
 *    FROM PERSON
 *   WHERE FIRST_NAME = :firstName
 *   ORDER BY ~%sortColumn%
 *  """);
 * try(Connection con = ...) {
 *   return sql.session(con)
 *       .setIdentifier("sortColumn", "SALARY")
 *       .prepareQuery()
 *       .bind("firstName", "John")
 *       .firstColumn();
 * }
 * }</pre></blockquote>
 */
public final class SQLQuery extends SQLStatement<SQLQuery> {

  private static final Logger LOG = LoggerFactory.getLogger(SQLQuery.class);

  private ResultSet resultSet;

  SQLQuery(PreparedStatement ps, AbstractSQLSession sql, SQLInfo sqlInfo) {
    super(ps, sql, sqlInfo);
  }

  /**
   * Executes the query and returns the raw JDBC {@link ResultSet}. If the query had
   * already been executed, it will not be executed again. Instead, the {@code ResultSet}
   * generated by the first call to {@code execute()} will be returned. Call
   * {@link SQLStatement#reset() reset()} to force the query to be re-executed.
   *
   * @return the {@code ResultSet} produced by the JDBC driver
   */
  public ResultSet execute() {
    try {
      executeSQL();
      return resultSet;
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  /**
   * Executes the query and returns the value of the first column of the first row. The
   * second time you call this method, you get the value of the first column of the second
   * row, and so on. If there are no (more) rows in the {@code ResultSet},
   * {@link Result#notAvailable() Result.notAvailable()} is returned. If the query had
   * already been executed, it will not be executed again. Call
   * {@link SQLStatement#reset() reset()} to force the query to be re-executed.
   *
   * @param <T> the type of the value to be returned
   * @param clazz the class of the value to be returned
   * @return a {@code Result} containing the value of the first column of the first row or
   *       {@link Result#notAvailable()} if there are no (more) rows in the
   *       {@code ResultSet}
   */
  public <T> Result<T> lookup(Class<T> clazz) {
    try {
      executeSQL();
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
   * Executes the query and returns the value of the first column of the first row as an
   * {@code Integer}. Equivalent to {@code lookup(Integer.class}). The second time you
   * call this method, you get the value of the first column of the second row, and so on.
   * If there are no (more) rows in the {@code ResultSet}, {@link Result#notAvailable()}
   * is returned. If the query had already been executed, it will not be executed again.
   * Call {@link SQLStatement#reset() reset()} to force the query to be re-executed.
   *
   * @return a {@code Result} containing the value of the first column of the first row or
   *       {@link Result#notAvailable()} if there are no (more) rows in the
   *       {@code ResultSet}
   */
  public Result<Integer> getInt() {
    try {
      executeSQL();
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
   * this method, you get the value of the first column of the second row, and so on. If
   * there are no (more) rows in the {@code ResultSet}, {@link Result#notAvailable()} is
   * returned. If the query had already been executed, it will not be executed again. Call
   * {@link SQLStatement#reset() reset()} to force the query to be re-executed.
   *
   * @return a {@code Result} containing the value of the first column of the first row or
   *       {@link Result#notAvailable()} if there are no (more) rows in the
   *       {@code ResultSet}
   */
  public Result<String> getString() {
    try {
      executeSQL();
      if (resultSet.next()) {
        return Result.of(resultSet.getString(1));
      }
      return Result.notAvailable();
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  /**
   * Executes the query and returns {@code true} if the query yielded at least one row;
   * {@code false} otherwise. If the query had already been executed, it will not be
   * executed again. Call {@link SQLStatement#reset() reset()} to force the query to be
   * re-executed. Note that this method will call {@link ResultSet#next()}, moving the
   * cursor to the next row in the {@code ResultSet}. This will influence the outcome of
   * the other methods of {@code SQLQuery}, and of subsequent calls to {@code exists()}
   * for that matter. Only call this method if the only thing you are interested in is
   * figuring out if a query yields a result, and nothing else.
   *
   * @return {@code true} if the query yielded at least one row; {@code false} otherwise
   */
  public boolean exists() {
    try {
      executeSQL();
      return resultSet.next();
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  /**
   * Executes the query and returns all values in the first column of the SELECT clause.
   * Equivalent to {@link #firstColumn(Class) firstColumn(String.class)}. If the query had
   * already been executed, it will not be executed again. Call
   * {@link SQLStatement#reset() reset()} to force the query to be re-executed.
   *
   * @return the values of the first column in the result set
   */
  public List<String> firstColumn() { return firstColumn(String.class); }

  /**
   * Executes the query and returns all values in the first column of the SELECT clause.
   * Equivalent to {@link #firstColumn(Class, int) firstColumn(clazz, 10)}. If the query
   * had already been executed, it will not be executed again. Call
   * {@link SQLStatement#reset() reset()} to force the query to be re-executed.
   *
   * @param <T> the desired type of the values
   * @param clazz the desired class of the values
   * @return the values of the first column in the result set
   */
  public <T> List<T> firstColumn(Class<T> clazz) { return firstColumn(clazz, 10); }

  /**
   * Executes the query and returns all values in the first column of the SELECT clause.
   * In other words, this method will retrieve all rows satisfying the WHERE and LIMIT
   * clauses (if any) and collect the values of the first column into a {@code List}. If
   * the query had already been executed, it will not be executed again. Call
   * {@link SQLStatement#reset() reset()} to force the query to be re-executed.
   *
   * @param <T> the desired type of the values
   * @param clazz the desired class of the values
   * @param sizeEstimate the expected number of rows
   * @return the values of the first column in the result set
   */
  public <T> List<T> firstColumn(Class<T> clazz, int sizeEstimate) {
    try {
      executeSQL();
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
   * Executes the query and returns a {@code MapExtractor} that you can use to convert the
   * rows in the {@link ResultSet} into {@code Map<String, Object>} pseudo-objects. If the
   * query had already been executed, it will not be executed again. Call
   * {@link SQLStatement#reset() reset()} to force the query to be re-executed.
   *
   * @return a {@code MapExtractor} that you can use to convert the rows in the
   *       {@link ResultSet} into {@code Map<String, Object>} pseudo objects.
   */
  public MapExtractor getExtractor() {
    try {
      executeSQL();
      return session
            .getSQL()
            .getMapExtractorFactory()
            .getExtractor(resultSet);
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  /**
   * Executes the query and returns a {@code BeanExtractor} that you can use to convert
   * the rows in the {@link ResultSet} into JavaBeans or records. If the query had already
   * been executed, it will not be executed again. Call
   * {@link SQLStatement#reset() reset()} to force the query to be re-executed.
   *
   * @param <T> the type of the JavaBeans or records
   * @param clazz the class of the JavaBeans or records
   * @return a {@code BeanExtractor} that you can use to convert the rows in the
   *       {@link ResultSet} into JavaBeans or records.
   */
  public <T> BeanExtractor<T> getExtractor(Class<T> clazz) {
    try {
      executeSQL();
      return session
            .getSQL()
            .getBeanExtractorFactory(clazz)
            .getExtractor(resultSet);
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  /**
   * Executes the query and returns a {@code BeanExtractor} that you can use to convert
   * the rows in the {@link ResultSet} into JavaBeans. The provided class must <i>not</i>
   * be a {@code record} type. If the query had already been executed, it will not be
   * executed again. Call {@link SQLStatement#reset() reset()} to force the query to be
   * re-executed.
   *
   * @param <T> the type of the JavaBeans (must <i>not</i> be a {@code record}
   *       type)
   * @param clazz the class of the JavaBeans
   * @param beanSupplier the supplier of the JavaBean instances. This would
   *       ordinarily be a method reference to the constructor of the JavaBean (like
   *       {@code Person::new})
   * @return a {@code BeanExtractor} that you can use to convert the rows in the
   *       {@link ResultSet} into JavaBeans.
   */
  public <T> BeanExtractor<T> getExtractor(Class<T> clazz, Supplier<T> beanSupplier) {
    try {
      executeSQL();
      return session
            .getSQL()
            .getBeanExtractorFactory(clazz, beanSupplier)
            .getExtractor(resultSet);
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
    } finally {
      resultSet = null;
    }
  }

  private void executeSQL() throws Throwable {
    if (resultSet == null) {
      applyBindings(ps);
      LOG.trace(Msg.EXECUTING_SQL, sqlInfo.sql());
      resultSet = ps.executeQuery();
    }
  }

}
