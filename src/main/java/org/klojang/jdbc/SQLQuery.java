package org.klojang.jdbc;

import org.klojang.check.aux.Result;
import org.klojang.check.fallible.FallibleFunction;
import org.klojang.jdbc.x.Msg;
import org.klojang.jdbc.x.Utils;
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
import java.util.Optional;
import java.util.function.Supplier;

import static java.lang.ref.Cleaner.Cleanable;
import static org.klojang.jdbc.x.Utils.CENTRAL_CLEANER;

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
@SuppressWarnings("resource")
public final class SQLQuery extends SQLStatement<SQLQuery> {

  private static final Logger LOG = LoggerFactory.getLogger(SQLQuery.class);

  private final ResultSetContainer result;
  private final Cleanable cleanable;

  SQLQuery(PreparedStatement stmt, AbstractSQLSession sql, SQLInfo sqlInfo) {
    super(stmt, sql, sqlInfo);
    this.result = new ResultSetContainer();
    this.cleanable = CENTRAL_CLEANER.register(this, result);
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
      ResultSet rs = executeIfNull();
      if (rs.next()) {
        int sqlType = rs.getMetaData().getColumnType(1);
        T val = ColumnReaderFactory.getInstance()
              .getReader(clazz, sqlType)
              .getValue(rs, 1, clazz);
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
      ResultSet rs = executeIfNull();
      if (rs.next()) {
        return Result.of(rs.getInt(1));
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
      ResultSet rs = executeIfNull();
      if (rs.next()) {
        return Result.of(rs.getString(1));
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
      return executeIfNull().next();
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
      ResultSet rs = executeIfNull();
      if (!rs.next()) {
        return Collections.emptyList();
      }
      int sqlType = rs.getMetaData().getColumnType(1);
      var reader = ColumnReaderFactory.getInstance().getReader(clazz, sqlType);
      List<T> list = new ArrayList<>(sizeEstimate);
      do {
        list.add(reader.getValue(rs, 1, clazz));
      } while (rs.next());
      return list;
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  /**
   * <p>Executes the query and returns a {@code MapExtractor} that you can use to convert
   * the rows in the {@link ResultSet} into {@code Map<String, Object>} pseudo-objects. If
   * the query had already been executed, it will not be executed again. Call
   * {@link SQLStatement#reset() reset()} to force the query to be re-executed.
   *
   * @return a {@code MapExtractor} that you can use to convert the rows in the
   *       {@link ResultSet} into {@code Map<String, Object>} pseudo objects.
   */
  public MapExtractor getExtractor() {
    try {
      ResultSet rs = executeIfNull();
      return session.getSQL().getMapExtractorFactory().getExtractor(rs);
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
   *
   * <p>Be wary of code like this:
   *
   * <blockquote><pre>{@code
   * BeanExtractor<Foo> extractor = SQL.simpleQuery(con, "SELECT * FROM FOO").getExtractor(Foo.class);
   * }</pre></blockquote>
   *
   * <p>This code does not close the temporary {@code SQLQuery} object from which you
   * retrieve the {@code BeanExtractor}, and hence you may think you can safely use the
   * {@code BeanExtractor}. But the {@code SQLQuery} object has gone out of scope by the
   * time you receive the {@code BeanExtractor}, and <i>Klojang JDBC</i> makes sure that
   * even if you do not close the {@code SQLQuery}, the JDBC resources associated with it
   * <i>will</i> get closed once the {@code SQLQuery} object gets garbage-collected.
   * Confusingly, you may sometimes get away with it and happily use the
   * {@code BeanExtractor} &#8212; when it takes some time for the garbage collector to
   * come around and reclaim the {@code SQLQuery} object. More often, though, the
   * {@code BeanExtractor} will operate on a {@code ResultSet} that has already been
   * closed. Request the {@code BeanExtractor} within a try-with-resource block created
   * for the {@code SQLQuery}, or at the very least make sure the {@code SQLQuery} stays
   * alive while you use the {@code BeanExtractor}.
   *
   * @param <T> the type of the JavaBeans or records
   * @param clazz the class of the JavaBeans or records
   * @return a {@code BeanExtractor} that you can use to convert the rows in the
   *       {@link ResultSet} into JavaBeans or records.
   */
  public <T> BeanExtractor<T> getExtractor(Class<T> clazz) {
    try {
      ResultSet rs = executeIfNull();
      return session.getSQL().getBeanExtractorFactory(clazz).getExtractor(rs);
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  /**
   * Executes the query and returns a {@code BeanExtractor} that you can use to convert
   * the rows in the {@link ResultSet} into JavaBeans. The provided class must
   * <i>not</i> be a {@code record} type. If the query had already been executed, it will
   * not be executed again. Call {@link SQLStatement#reset() reset()} to force the query
   * to be re-executed.
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
      ResultSet rs = executeIfNull();
      return session.getSQL()
            .getBeanExtractorFactory(clazz, beanSupplier)
            .getExtractor(rs);
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  /**
   * Executes the query and converts the first row in the {@code ResultSet} into an object
   * of type {@code <T>} using the specified conversion function. If the query yielded an
   * empty {@code ResultSet}, the method returns an empty {@link Optional}. If the query
   * had already been executed, it will not be executed again, and this method will
   * convert the next row in the {@code ResultSet}. You can keep calling this method until
   * an empty {@code Optional} is returned. Call {@link SQLStatement#reset() reset()} to
   * force the query to be re-executed.
   *
   * @param converter the conversion function. Do not call
   *       {@link ResultSet#next() next()} or {@link ResultSet#close() close()} on the
   *       {@code ResultSet} passed to the conversion function. This is the responsibility
   *       of the {@code SQLQuery} object.
   * @param <T> the type of the objects into which the rows in the {@code ResultSet}
   *       are converted
   * @return an {@link Optional} containing the object created from the current row in the
   *       {@code ResultSet} or an empty {@code Optional} if there are no (more) rows in
   *       the {@code ResultSet}
   */
  public <T> Optional<T> extract(FallibleFunction<ResultSet, T, SQLException> converter) {
    try {
      ResultSet rs = executeIfNull();
      if (rs.next()) {
        return Optional.of(converter.apply(rs));
      }
      return Optional.empty();
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  /**
   * <p>Executes the query and converts at most {@code limit} rows in the
   * {@code ResultSet} into objects of type {@code <T>} using the specified conversion
   * function. If the query had already been executed, it will not be executed again.
   * Thus, you can call this method again to retrieve and convert the next batch of rows
   * from the {@code ResultSet}. Call {@link SQLStatement#reset() reset()} to force the
   * query to be re-executed.
   *
   * <p>This method is especially useful if the conversion process is too complicated to
   * be carried out by a regular {@link BeanExtractor}, or if you want to eliminate all
   * dynamic invocation (reflection,
   * {@linkplain java.lang.invoke.MethodHandle method handles}) from the conversion
   * process.
   *
   * @param limit the maximum number of rows to extract from the {@code ResultSet}
   * @param converter the conversion function. Do not call
   *       {@link ResultSet#next() next()} or {@link ResultSet#close() close()} on the
   *       {@code ResultSet} passed to the conversion function. This is the responsibility
   *       of the {@code SQLQuery} object.
   * @param <T> the type of the objects into which the rows in the {@code ResultSet}
   *       are converted
   * @return a {@code List} of the objects created by the conversion function
   */
  public <T> List<T> extract(int limit,
        FallibleFunction<ResultSet, T, SQLException> converter) {
    try {
      ResultSet rs = executeIfNull();
      List<T> beans = new ArrayList<>(limit);
      for (int i = 0; i < limit && rs.next(); ++i) {
        beans.add(converter.apply(rs));
      }
      return beans;
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  /**
   * Equivalent to {@link #extractAll(int, FallibleFunction) extractAll(10, converter)}.
   *
   * @param converter the conversion function. Do not call
   *       {@link ResultSet#next() next()} or {@link ResultSet#close() close()} on the
   *       {@code ResultSet} passed to the conversion function. This is the responsibility
   *       of the {@code SQLQuery} object.
   * @param <T> the type of the objects into which the rows in the {@code ResultSet}
   *       are converted
   * @return a {@code List} of the objects created by the conversion function
   */
  public <T> List<T> extractAll(FallibleFunction<ResultSet, T, SQLException> converter) {
    return extractAll(10, converter);
  }

  /**
   * <p>Executes the query and converts the rows in the {@code ResultSet} into objects of
   * type {@code <T>} using the specified conversion function. If the query had already
   * been executed, it will not be executed again. Thus, after a call to this method, you
   * should really close the {@code SQLQuery} instance or call
   * {@link SQLStatement#reset() reset()} to force the query to be re-executed.
   *
   * <p>This method is especially useful if the conversion process is too complicated to
   * be carried out by a regular {@link BeanExtractor}, or if you want to eliminate all
   * dynamic invocation (reflection,
   * {@linkplain java.lang.invoke.MethodHandle method handles}) from the conversion
   * process.
   *
   * @param sizeEstimate an estimate of the number of rows in the {@code ResultSet}
   * @param converter the conversion function. Do not call
   *       {@link ResultSet#next() next()} or {@link ResultSet#close() close()} on the
   *       {@code ResultSet} passed to the conversion function. This is the responsibility
   *       of the {@code SQLQuery} object.
   * @param <T> the type of the objects into which the rows in the {@code ResultSet}
   *       are converted
   * @return a {@code List} of the objects created by the conversion function
   */
  public <T> List<T> extractAll(int sizeEstimate,
        FallibleFunction<ResultSet, T, SQLException> converter) {
    try {
      ResultSet rs = executeIfNull();
      List<T> beans = new ArrayList<>(sizeEstimate);
      while (rs.next()) {
        beans.add(converter.apply(rs));
      }
      return beans;
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  /**
   * <p>Executes the query and returns the result. If the query had already been
   * executed, it will not be executed again. Instead, the {@code ResultSet} generated by
   * the first call to {@code execute()} will be returned. Call
   * {@link SQLStatement#reset() reset()} to force the query to be re-executed. Since the
   * returned {@code ResultSet} is the same one that underpins this instance, be careful
   * what you do with it. For example, if you {@code close} the {@code ResultSet}, but
   * then continue to use this instance, an exception is almost guaranteed to follow.
   * <i>Klojang JDBC</i> does not protect itself against such unintended usage. Also, be
   * wary of code like this:
   *
   * <blockquote><pre>{@code
   * ResultSet rs = SQL.simpleQuery(con, "SELECT * FROM FOO").getResultSet();
   * }</pre></blockquote>
   *
   * <p>This code does not close the temporary {@code SQLQuery} object from which you
   * retrieve the {@code ResultSet}, and hence you may think you can safely use the
   * {@code ResultSet}. But the {@code SQLQuery} object goes out of scope once you have
   * received the {@code ResultSet}, and <i>Klojang JDBC</i> makes sure that even if you
   * do not close the {@code SQLQuery}, the JDBC resources associated with it
   * <i>will</i> get closed once the {@code SQLQuery} object gets garbage-collected.
   * Confusingly, you may sometimes get away with it and happily use the {@code ResultSet}
   * &#8212; when it takes some time for the garbage collector to come around and reclaim
   * the {@code SQLQuery} object. More often, though, the {@code ResultSet} will be dead
   * on arrival.
   *
   * @return the query result
   */
  public ResultSet getResultSet() {
    try {
      return executeIfNull();
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  @Override
  public void close() {
    cleanable.clean();
    super.close();
  }

  @Override
  void initialize() {
    try {
      if (result.get() != null) {
        result.get().close();
      }
      stmt().clearParameters();
    } catch (SQLException e) {
      throw Utils.wrap(e);
    } finally {
      result.set(null);
    }
  }

  private ResultSet executeIfNull() throws Throwable {
    ResultSet rs = result.get();
    if (rs == null) {
      applyBindings(stmt());
      LOG.trace(Msg.EXECUTING_SQL, sqlInfo.sql());
      rs = stmt().executeQuery();
      result.set(rs);
    }
    return rs;
  }


  private static class ResultSetContainer implements Runnable {

    private ResultSet rs;

    ResultSet get() { return rs; }

    void set(ResultSet rs) { this.rs = rs; }

    @Override
    public void run() {
      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException e) {
          // ...
        }
      }
    }
  }

}
