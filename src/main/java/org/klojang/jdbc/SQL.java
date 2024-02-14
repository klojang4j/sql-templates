package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.SQLCache;
import org.klojang.jdbc.x.Utils;

import java.sql.Connection;

import static org.klojang.jdbc.x.Strings.*;

/**
 * <p>Encapsulates a user-provided SQL statement. <i>Klojang JDBC</i> provides three
 * implementations of the {@code SQL} interface. These implementations are hidden from
 * view. Instances of them are obtained via static factory methods on the {@code SQL}
 * interface itself. The implementations cater to different levels of parametrization of
 * the SQL.
 *
 * <h2>Named Parameters</h2>
 * <p>The simplest implementation supports the use of named parameters within SQL query
 * strings. For example:
 *
 * <blockquote><pre>{@code
 * SELECT *
 *   FROM EMPLOYEE
 *  WHERE FIRST_NAME = :firstName
 *    AND LAST_NAME = :lastName
 *  LIMIT :from, :batchSize
 * }</pre></blockquote>
 *
 * <p>Named parameters are placeholders for, and will ultimately be replaced by regular
 * JDBC parameters (that is: question marks). Thus, named parameters can and should be
 * used wherever you would ordinarily use regular JDBC parameters. An instance of this
 * implementation can be obtained via the {@link #simple(String) SQL.simple()} method. You
 * should also preferably use this implementation for completely static SQL.
 *
 * <h2>SQL Templates</h2>
 * <p>A classic example of something that you often want to, but cannot parametrize with
 * JDBC are the column(s) in the ORDER BY clause. <i>Klojang JDBC</i> lets you do this by
 * providing SQL in the form of a
 * <b><a
 * href="https://klojang4j.github.io/klojang-templates/api/org.klojang.templates/module-summary.html">Klojang
 * template</a></b>. That is: SQL that contains <i>Klojang Templates</i> variables. For
 * example:
 *
 * <blockquote><pre>{@code
 * SELECT *
 *   FROM EMPLOYEE
 *  WHERE FIRST_NAME = :firstName
 *    AND LAST_NAME = :lastName
 *  ORDER BY ~%sortColumn% ~%sortOrder%
 *  LIMIT :from, :batchSize
 * }</pre></blockquote>
 *
 * <p>(So, named parameters look like this: <b>{@code :fooBar}</b>. <i>Klojang
 * Templates</i> variables look like this: <b>{@code ~%fooBar%}</b>.)
 *
 * <p>The workflow is as follows: <i>Klojang Templates</i> variables are set in the
 * {@link SQLSession} obtained from the {@code SQL} object; named parameters are set
 * (a.k.a. "bound") in the {@link SQLStatement} obtained from the {@code SQLSession}.
 * Since "simple" SQL is not allowed to contain <i>Klojang Templates</i> variables, the
 * {@code SQL} interface contains a few convenience methods that yield a
 * {@link SQLStatement} object straight away, even though under the hood they still create
 * a {@link SQLSession} object.
 *
 * <p>SQL templates can be created using the {@link #template(String) SQL.template()}
 * method. <i>Klojang JDBC</i> is agnostic about, and has no opinion on what gets
 * parametrized this way. You could also parametrize the table(s) in the FROM clause for
 * example. <i>Klojang JDBC</i> lets you do this in a safe manner by providing the
 * {@link SQLSession#setIdentifier(String, String) setIdentifier()} method. This method
 * protects you against SQL injection by applying the quoting rules of the target database
 * to the provided identifier.
 *
 * <h2>SQL Skeletons</h2>
 * <p>The implementation obtained via the {@link #skeleton(String) SQL.skeleton()}
 * method allows for very dynamically generated SQL. As with SQL templates, the SQL is
 * provided in the form of a Klojang template (that is: it may contain <i>Klojang
 * Templates</i> variables). However, SQL skeletons explicitly allow you to set template
 * variables to SQL fragments that again contain named parameters. <i>Klojang JDBC</i>
 * will register them, and you can bind them just like the named parameters in the SQL
 * skeleton itself. This is not possible with the implementation returned by
 * {@code SQL.template()}, since this implementation will immediately (upon instantiation)
 * extract all parameters from the original SQL string &#8212; and leave it at that. With
 * SQL skeletons, parameter extraction is delayed to the very last moment, just before you
 * retrieve a {@link SQLStatement} from the {@code SQLSession}. This makes SQL skeletons
 * somewhat less efficient, but more dynamic than SQL templates.
 *
 *
 * <blockquote><pre>{@code
 * SQL sql = SQL.skeleton("""
 *     SELECT *
 *       FROM EMPLOYEE A
 *       ~%joinDepartment%
 *      ORDER BY ~%sortColumn%
 *      LIMIT :from, :batchSize
 *     """;
 *
 * try(Connection con = ...) {
 *   SQLSession session = sql.session(con);
 *   session.set("sortColumn", "A.SALARY");
 *   if(departmentName != null) {
 *     session.set("joinDepartment",
 *         "JOIN DEPARTMENT B ON (A.DEPARTMENT_ID = B.ID AND B.NAME = :dept)");
 *   }
 *   try(SQLQuery query = session.prepareQuery()) {
 *     query.bind("from", 0).bind("batchSize", 20);
 *     if(departmentName != null) {
 *       query.bind("dept", departmentName);
 *     }
 *     List<Employee> emps = query.getExtractor(Employee.class).extractAll();
 *   }
 * }
 * }</pre></blockquote>
 *
 * @see org.klojang.templates.Template
 * @see org.klojang.templates.RenderSession
 */
public sealed interface SQL permits AbstractSQL {

  /**
   * Returns a {@code SQL} implementation that supports neither named parameters nor
   * <i>Klojang Templates</i> variables. In other words, it only supports completely
   * static SQL. This method returns the same {@code SQL} implementation as the one
   * returned by {@link #simple(String) SQL.simple()}, but does so under the
   * <i>assumption</i> that the SQL does not contain any named parameters, thus saving on
   * the cost of parsing the SQL in order to extract the named parameters.
   *
   * @param sql the SQL statement
   * @return a {@code SQL} implementation that supports neither named parameters nor
   *       <i>Klojang Templates</i> variables
   */
  static SQL staticSQL(String sql) {
    Check.notNull(sql, SQL_ARGUMENT);
    return new SimpleSQL(sql, true, noSessionConfig());
  }

  /**
   * Returns a {@code SQL} implementation that supports neither named parameters nor
   * <i>Klojang Templates</i> variables. In other words, it only supports completely
   * static SQL. The SQL is read from the specified classpath resource. The resulting
   * {@code SQL} instance is cached and returned upon every subsequent call with the same
   * {@code clazz} and {@code path} arguments. This method returns the same {@code SQL}
   * implementation as the one returned by {@link #simple(String) SQL.simple()}, but does
   * so under the <i>assumption</i> that the SQL does not contain any named parameters,
   * thus saving on the cost of parsing the SQL in order to extract the named parameters.
   *
   * @param clazz a {@code Class} object that provides access to the SQL file by
   *       calling {@code getResourceAsStream} on it
   * @param path the location of the SQL file
   * @return a {@code SQL} implementation that supports neither named parameters nor
   *       <i>Klojang Templates</i> variables
   */
  static SQL staticSQL(Class<?> clazz, String path) {
    return SQLCache.get(clazz, path, SQL::staticSQL);
  }

  /**
   * Returns a {@code SQL} implementation that allows for named parameters, but not for
   * <i>Klojang Templates</i> variables.
   *
   * @param sql the SQL statement
   * @return a {@code SQL} implementation that allows for named parameters, but not for
   *       <i>Klojang Templates</i> variables
   */
  static SQL simple(String sql) {
    Check.notNull(sql, SQL_ARGUMENT);
    return simple(sql, noSessionConfig());
  }

  /**
   * Returns a {@code SQL} implementation that allows for named parameters, but not for
   * <i>Klojang Templates</i> variables. The SQL is read from the specified classpath
   * resource. The resulting {@code SQL} instance is cached and returned upon every
   * subsequent call with the same {@code clazz} and {@code path} arguments.
   *
   * @param clazz a {@code Class} object that provides access to the SQL file by
   *       calling {@code getResourceAsStream} on it
   * @param path the location of the SQL file
   * @return a {@code SQL} implementation that allows for named parameters, but not for
   *       <i>Klojang Templates</i> variables
   */
  static SQL simple(Class<?> clazz, String path) {
    return SQLCache.get(clazz, path, SQL::simple);
  }

  /**
   * Returns a {@code SQL} implementation that allows for named parameters, but not for
   * <i>Klojang Templates</i> variables.
   *
   * @param sql the SQL statement
   * @param config a {@code SessionConfig} object that allows you to fine-tune how
   *       values are bound into the underlying {@link java.sql.PreparedStatement}
   * @return an instance of an {@code SQL} implementation that behaves as described above
   */
  static SQL simple(String sql, SessionConfig config) {
    Check.notNull(sql, SQL_ARGUMENT);
    Check.notNull(config, CONFIG);
    return new SimpleSQL(sql, false, config);
  }

  /**
   * Returns a {@code SQL} implementation that allows for named parameters, but not for
   * <i>Klojang Templates</i> variables. The SQL is read from the specified classpath
   * resource. The resulting {@code SQL} instance is cached and returned upon every
   * subsequent call with the same {@code clazz} and {@code path} arguments.
   *
   * @param clazz a {@code Class} object that provides access to the SQL file by
   *       calling {@code getResourceAsStream} on it
   * @param path the location of the SQL file
   * @param config a {@code SessionConfig} object that allows you to fine-tune how
   *       values are bound into the underlying {@link java.sql.PreparedStatement}
   * @return a {@code SQL} implementation that allows for named parameters, but not for
   *       <i>Klojang Templates</i> variables
   */
  static SQL simple(Class<?> clazz, String path, SessionConfig config) {
    return SQLCache.get(clazz, path, SQL::simple);
  }

  /**
   * Convenience method for SQL SELECT statements that do not contain <i>Klojang
   * Templates</i> variables. Equivalent to:
   *
   * <blockquote><pre>{@code
   * SQL.simple(sql, SessionConfig.DEFAULT).session(con).prepareQuery();
   * }</pre></blockquote>
   *
   * @param con the JDBC connection to use
   * @param sql the SQL SELECT statement
   * @return an {@code SQLQuery} instance that allows you to bind the named parameters in
   *       the SQL (if present) and then execute it
   * @see SQLSession#prepareQuery()
   */
  static SQLQuery simpleQuery(Connection con, String sql) {
    return simpleQuery(con, sql, noSessionConfig());
  }

  /**
   * Convenience method for SQL SELECT statements that do not contain <i>Klojang
   * Templates</i> variables. Equivalent to:
   *
   * <blockquote><pre>{@code
   * SQL.simple(sql, config).session(con).prepareQuery();
   * }</pre></blockquote>
   *
   * @param con the JDBC connection to use
   * @param sql the SQL SELECT statement
   * @param config a {@code SessionConfig} object that allows you to fine-tune how
   *       values are bound into the underlying {@link java.sql.PreparedStatement}
   * @return an {@code SQLQuery} instance that allows you to bind the named parameters in
   *       the SQL (if present) and then execute it
   * @see SQLSession#prepareQuery()
   */
  static SQLQuery simpleQuery(Connection con, String sql, SessionConfig config) {
    Check.notNull(con, CONNECTION);
    Check.notNull(sql, SQL_ARGUMENT);
    Check.notNull(config, CONFIG);
    return simple(sql, config).session(con).prepareQuery();
  }

  /**
   * Convenience method for SQL INSERT statements that do not contain <i>Klojang
   * Templates</i> variables. Equivalent to:
   *
   * <blockquote><pre>{@code
   * SQL.simple(sql, SessionConfig.DEFAULT).session(con).prepareInsert();
   * }</pre></blockquote>
   *
   * @param con the JDBC connection to use
   * @param sql the SQL INSERT statement
   * @return an {@code SQLInsert} instance that allows you to bind the named parameters in
   *       the SQL (if present) and then execute it
   * @see SQLSession#prepareInsert()
   */
  static SQLInsert simpleInsert(Connection con, String sql) {
    return simple(sql, noSessionConfig()).session(con).prepareInsert();
  }

  /**
   * Convenience method for SQL INSERT statements that do not contain <i>Klojang
   * Templates</i> variables. Equivalent to:
   *
   * <blockquote><pre>{@code
   * SQL.simple(sql, config).session(con).prepareInsert();
   * }</pre></blockquote>
   *
   * @param con the JDBC connection to use
   * @param sql the SQL INSERT statement
   * @param retrieveKeys whether to retrieve the keys that were generated by the
   *       database
   * @param config a {@code SessionConfig} object that allows you to fine-tune how
   *       values are bound into the underlying {@link java.sql.PreparedStatement}
   * @return an {@code SQLInsert} instance that allows you to bind the named parameters in
   *       the SQL (if present) and then execute it
   * @see SQLSession#prepareInsert(boolean)
   */
  static SQLInsert simpleInsert(Connection con,
        String sql,
        boolean retrieveKeys,
        SessionConfig config) {
    Check.notNull(con, CONNECTION);
    Check.notNull(sql, SQL_ARGUMENT);
    Check.notNull(config, CONFIG);
    return simple(sql, config).session(con).prepareInsert(retrieveKeys);
  }

  /**
   * Convenience method for SQL UPDATE statements that do not contain <i>Klojang
   * Templates</i> variables. Equivalent to:
   *
   * <blockquote><pre>{@code
   * SQL.simple(sql, SessionConfig.DEFAULT).session(con).prepareUpdate();
   * }</pre></blockquote>
   *
   * @param con the JDBC connection to use
   * @param sql the SQL UPDATE statement
   * @return an {@code SQLUpdate} instance that allows you to bind the named parameters in
   *       the SQL (if present) and then execute it
   * @see SQLSession#prepareUpdate()
   */
  static SQLUpdate simpleUpdate(Connection con, String sql) {
    return simpleUpdate(con, sql, noSessionConfig());
  }

  /**
   * Convenience method for SQL UPDATE statements that do not contain <i>Klojang
   * Templates</i> variables. Equivalent to:
   *
   * <blockquote><pre>{@code
   * SQL.simple(sql, config).session(con).prepareUpdate();
   * }</pre></blockquote>
   *
   * @param con the JDBC connection to use
   * @param sql the SQL UPDATE statement
   * @param config a {@code SessionConfig} object that allows you to fine-tune how
   *       values are bound into the underlying {@link java.sql.PreparedStatement}
   * @return an {@code SQLUpdate} instance that allows you to bind the named parameters in
   *       the SQL (if present) and then execute it
   * @see SQLSession#prepareUpdate()
   */
  static SQLUpdate simpleUpdate(Connection con, String sql, SessionConfig config) {
    Check.notNull(con, CONNECTION);
    Check.notNull(sql, SQL_ARGUMENT);
    Check.notNull(config, CONFIG);
    return simple(sql, config).session(con).prepareUpdate();
  }


  /**
   * Returns a {@code SQL} implementation that allows for named parameters and
   * <i>Klojang Templates</i> variables.
   *
   * @param sql the SQL statement
   * @return a {@code SQL} implementation that allows for named parameters and
   *       <i>Klojang Templates</i> variables
   */
  static SQL template(String sql) {
    return template(sql, noSessionConfig());
  }

  /**
   * Returns a {@code SQL} implementation that allows for named parameters and
   * <i>Klojang Templates</i> variables. The SQL is read from the specified classpath
   * resource. The resulting {@code SQL} instance is cached and returned upon every
   * subsequent call with the same {@code clazz} and {@code path} arguments.
   *
   * @param clazz a {@code Class} object that provides access to the SQL file by
   *       calling {@code getResourceAsStream} on it
   * @param path the location of the SQL file
   * @return a {@code SQL} implementation that allows for named parameters and
   *       <i>Klojang Templates</i> variables
   */
  static SQL template(Class<?> clazz, String path) {
    return SQLCache.get(clazz, path, SQL::template);
  }


  /**
   * Returns a {@code SQL} implementation that allows for named parameters and
   * <i>Klojang Templates</i> variables.
   *
   * @param sql the SQL statement
   * @param config a {@code SessionConfig} object that allows you to fine-tune how
   *       values are bound into the underlying {@link java.sql.PreparedStatement}
   * @return a {@code SQL} implementation that allows for named parameters and
   *       <i>Klojang Templates</i> variables
   */
  static SQL template(String sql, SessionConfig config) {
    Check.notNull(sql, SQL_ARGUMENT);
    Check.notNull(config, CONFIG);
    return new SQLTemplate(sql, config);
  }

  /**
   * Returns a {@code SQL} implementation that allows for named parameters and
   * <i>Klojang Templates</i> variables. The SQL is read from the specified classpath
   * resource. The resulting {@code SQL} instance is cached and returned upon every
   * subsequent call with the same {@code clazz} and {@code path} arguments.
   *
   * @param clazz a {@code Class} object that provides access to the SQL file by
   *       calling {@code getResourceAsStream} on it
   * @param path the location of the SQL file
   * @param config a {@code SessionConfig} object that allows you to fine-tune how
   *       values are bound into the underlying {@link java.sql.PreparedStatement}
   * @return a {@code SQL} implementation that allows for named parameters and
   *       <i>Klojang Templates</i> variables
   */
  static SQL template(Class<?> clazz, String path, SessionConfig config) {
    return SQLCache.get(clazz, path, config, SQL::template);
  }


  /**
   * Returns a {@code SQL} implementation that allows for named parameters and
   * <i>Klojang Templates</i> variables. The template variables may be set to SQL
   * fragments that again contain named parameters.
   *
   * @param sql the SQL statement
   * @return a {@code SQL} implementation that allows for named parameters and
   *       <i>Klojang Templates</i> variables
   */
  static SQL skeleton(String sql) {
    return skeleton(sql, noSessionConfig());
  }


  /**
   * Returns a {@code SQL} implementation that allows for named parameters and
   * <i>Klojang Templates</i> variables. The template variables may be set to SQL
   * fragments that again contain named parameters. The SQL is read from the specified
   * classpath resource. The resulting {@code SQL} instance is cached and returned upon
   * every subsequent call with the same {@code clazz} and {@code path} arguments.
   *
   * @param clazz a {@code Class} object that provides access to the SQL file by
   *       calling {@code getResourceAsStream} on it
   * @param path the location of the SQL file
   * @return a {@code SQL} implementation that allows for named parameters and
   *       <i>Klojang Templates</i> variables
   */
  static SQL skeleton(Class<?> clazz, String path) {
    return SQLCache.get(clazz, path, SQL::skeleton);
  }


  /**
   * Returns a {@code SQL} implementation that allows for named parameters and
   * <i>Klojang Templates</i> variables. The template variables may be set to SQL
   * fragments that again contain named parameters.
   *
   * @param sql the SQL statement
   * @param config a {@code SessionConfig} object that allows you to fine-tune how
   *       values are bound into the underlying {@link java.sql.PreparedStatement}
   * @return a {@code SQL} implementation that allows for named parameters and
   *       <i>Klojang Templates</i> variables
   */
  static SQL skeleton(String sql, SessionConfig config) {
    Check.notNull(sql, SQL_ARGUMENT);
    Check.notNull(config, CONFIG);
    return new SQLSkeleton(sql, config);
  }

  /**
   * Returns a {@code SQL} implementation that allows for named parameters and
   * <i>Klojang Templates</i> variables. The template variables may be set to SQL
   * fragments that again contain named parameters. The SQL is read from the specified
   * classpath resource. The resulting {@code SQL} instance is cached and returned upon
   * every subsequent call with the same {@code clazz} and {@code path} arguments.
   *
   * @param clazz a {@code Class} object that provides access to the SQL file by
   *       calling {@code getResourceAsStream} on it
   * @param path the location of the SQL file
   * @param config a {@code SessionConfig} object that allows you to fine-tune how
   *       values are bound into the underlying {@link java.sql.PreparedStatement}
   * @return a {@code SQL} implementation that allows for named parameters and
   *       <i>Klojang Templates</i> variables
   */
  static SQL skeleton(Class<?> clazz, String path, SessionConfig config) {
    return SQLCache.get(clazz, path, config, SQL::skeleton);
  }


  /**
   * Returns an {@link InsertBuilder} that enables you to easily configure an SQL INSERT
   * statement.
   *
   * @return an {@code SQLInsertBuilder} that enables you to easily configure an SQL
   *       INSERT statement
   */
  static InsertBuilder insert() {
    return new InsertBuilder();
  }

  /**
   * Returns an {@link BatchInsertBuilder} that enables you to easily configure large
   * batch inserts.
   *
   * @return an {@code SQLBatchInsertBuilder} that enables you to easily configure large
   *       batch inserts.
   */
  static BatchInsertBuilder batchInsert() {
    return new BatchInsertBuilder();
  }

  /**
   * Returns a special string wrapper object whose type signals to <i>Klojang JDBC</i>
   * that the specified string is to be treated as a native SQL expression and hence must
   * not be quoted or escaped.
   *
   * @param expression the SQL expression
   * @return a special string wrapper object whose type signals to <i>Klojang JDBC</i>
   *       that the specified string is to be treated as a native SQL expression
   * @see java.sql.Statement#enquoteLiteral(String)
   * @see Quoter
   */
  static SQLExpression expression(String expression) {
    Check.notNull(expression);
    return new SQLExpression(expression);
  }

  /**
   * Returns a {@code SQLSession} that allows you to execute the SQL query.
   *
   * @param con the JDBC connection to use
   * @return a {@code SQLSession} that allows you to execute the SQL query
   */
  SQLSession session(Connection con);

  private static SessionConfig noSessionConfig() {
    return Utils.DEFAULT_CONFIG;
  }


}
