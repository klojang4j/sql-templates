package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.SQLCache;

import java.sql.Connection;

import static org.klojang.jdbc.x.Strings.*;
import static org.klojang.jdbc.x.Utils.DEFAULT_CONFIG;

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
   * static SQL. This method actually returns the same {@code SQL} implementation as the
   * one returned by {@link #simple(String) SQL.simple()}, but simply <i>assumes</i> that
   * the SQL does not contain named parameters, thus saving on the cost of parsing the SQL
   * in order to extract them.
   *
   * @param sql the SQL statement
   * @return a {@code SQL} implementation that supports neither named parameters nor
   *       <i>Klojang Templates</i> variables
   */
  static SQL staticSQL(String sql) {
    Check.notNull(sql, SQL_ARGUMENT);
    return new SimpleSQL(sql, DEFAULT_CONFIG, true);
  }

  /**
   * Returns a {@code SQL} implementation that supports neither named parameters nor
   * <i>Klojang Templates</i> variables. In other words, it only supports completely
   * static SQL. This method actually returns the same {@code SQL} implementation as the
   * one returned by {@link #simple(String) SQL.simple()}, but simply <i>assumes</i> that
   * the SQL does not contain named parameters, thus saving on the cost of parsing the SQL
   * in order to extract them.
   *
   * @param config a {@code SessionConfig} object that allows you to fine-tune how
   *       <i>Klojang JDBC</i> operates
   * @param sql the SQL statement
   * @return a {@code SQL} implementation that supports neither named parameters nor
   *       <i>Klojang Templates</i> variables
   */
  static SQL staticSQL(SessionConfig config, String sql) {
    Check.notNull(config, CONFIG);
    Check.notNull(sql, SQL_ARGUMENT);
    return new SimpleSQL(sql, config, true);
  }

  /**
   * Returns a {@code SQL} implementation that supports neither named parameters nor
   * <i>Klojang Templates</i> variables. In other words, it only supports completely
   * static SQL. The SQL is read from the specified classpath resource. The resulting
   * {@code SQL} instance is cached and returned upon every subsequent call with the same
   * {@code clazz} and {@code sqlFile} arguments. This method returns the same {@code SQL}
   * implementation as the one returned by {@link #simple(String) SQL.simple()}, but does
   * so under the <i>assumption</i> that the SQL does not contain any named parameters,
   * thus saving on the cost of parsing the SQL in order to extract the named parameters.
   *
   * @param clazz a {@code Class} object that provides access to the SQL file by
   *       calling {@code clazz.getResourceAsStream(sqlFile)}
   * @param sqlFile the location of the SQL file
   * @return a {@code SQL} implementation that supports neither named parameters nor
   *       <i>Klojang Templates</i> variables
   */
  static SQL staticSQL(Class<?> clazz, String sqlFile) {
    return SQLCache.get(clazz, sqlFile, SQL::staticSQL);
  }

  static SQL staticSQL(SessionConfig config, Class<?> clazz, String sqlFile) {
    return SQLCache.get(clazz, sqlFile, config, SQL::staticSQL);
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
    return simple(DEFAULT_CONFIG, sql);
  }

  /**
   * Returns a {@code SQL} implementation that allows for named parameters, but not for
   * <i>Klojang Templates</i> variables. The SQL is read from the specified classpath
   * resource. The resulting {@code SQL} instance is cached and returned upon every
   * subsequent call with the same {@code clazz} and {@code sqlFile} arguments.
   *
   * @param clazz a {@code Class} object that provides access to the SQL file by
   *       calling {@code clazz.getResourceAsStream(sqlFile)}
   * @param sqlFile the location of the SQL file
   * @return a {@code SQL} implementation that allows for named parameters, but not for
   *       <i>Klojang Templates</i> variables
   */
  static SQL simple(Class<?> clazz, String sqlFile) {
    return SQLCache.get(clazz, sqlFile, SQL::simple);
  }

  /**
   * Returns a {@code SQL} implementation that allows for named parameters, but not for
   * <i>Klojang Templates</i> variables.
   *
   * @param config a {@code SessionConfig} object that allows you to fine-tune how
   *       <i>Klojang JDBC</i> operates
   * @param sql the SQL statement
   * @return an instance of an {@code SQL} implementation that behaves as described above
   */
  static SQL simple(SessionConfig config, String sql) {
    Check.notNull(config, CONFIG);
    Check.notNull(sql, SQL_ARGUMENT);
    return new SimpleSQL(sql, config, false);
  }

  /**
   * Returns a {@code SQL} implementation that allows for named parameters, but not for
   * <i>Klojang Templates</i> variables. The SQL is read from the specified classpath
   * resource. The resulting {@code SQL} instance is cached and returned upon every
   * subsequent call with the same {@code clazz} and {@code sqlFile} arguments.
   *
   * @param config a {@code SessionConfig} object that allows you to fine-tune how
   *       <i>Klojang JDBC</i> operates
   * @param clazz a {@code Class} object that provides access to the SQL file by
   *       calling {@code clazz.getResourceAsStream(sqlFile)}
   * @param sqlFile the location of the SQL file
   * @return a {@code SQL} implementation that allows for named parameters, but not for
   *       <i>Klojang Templates</i> variables
   */
  static SQL simple(SessionConfig config, Class<?> clazz, String sqlFile) {
    return SQLCache.get(clazz, sqlFile, SQL::simple);
  }

  /**
   * Convenience method for SQL SELECT statements that do not contain <i>Klojang
   * Templates</i> variables. Equivalent to:
   *
   * <blockquote><pre>{@code
   * SQL.simple(SessionConfig.getDefaultConfig(), sql).session(con).prepareQuery();
   * }</pre></blockquote>
   *
   * @param con the JDBC connection to use
   * @param sql the SQL SELECT statement
   * @return a {@code SQLQuery} instance that allows you to bind the named parameters in
   *       the SQL (if present) and then execute it
   * @see SQLSession#prepareQuery()
   */
  static SQLQuery simpleQuery(Connection con, String sql) {
    return simpleQuery(con, DEFAULT_CONFIG, sql);
  }

  /**
   * Convenience method for SQL SELECT statements that do not contain <i>Klojang
   * Templates</i> variables. Equivalent to:
   *
   * <blockquote><pre>{@code
   * SQL.simple(config, sql).session(con).prepareQuery();
   * }</pre></blockquote>
   *
   * @param con the JDBC connection to use
   * @param config a {@code SessionConfig} object that allows you to fine-tune how
   *       <i>Klojang JDBC</i> operates
   * @param sql the SQL SELECT statement
   * @return a {@code SQLQuery} instance that allows you to bind the named parameters in
   *       the SQL (if present) and then execute it
   * @see SQLSession#prepareQuery()
   */
  static SQLQuery simpleQuery(Connection con, SessionConfig config, String sql) {
    Check.notNull(con, CONNECTION);
    Check.notNull(config, CONFIG);
    Check.notNull(sql, SQL_ARGUMENT);
    return simple(config, sql).session(con).prepareQuery();
  }

  /**
   * Convenience method for SQL INSERT statements that do not contain <i>Klojang
   * Templates</i> variables. Equivalent to:
   *
   * <blockquote><pre>{@code
   * SQL.simple(SessionConfig.getDefaultConfig(), sql).session(con).prepareInsert();
   * }</pre></blockquote>
   *
   * @param con the JDBC connection to use
   * @param sql the SQL INSERT statement
   * @return a {@code SQLInsert} instance that allows you to bind the named parameters in
   *       the SQL (if present) and then execute it
   * @see SQLSession#prepareInsert()
   */
  static SQLInsert simpleInsert(Connection con, String sql) {
    return simple(DEFAULT_CONFIG, sql).session(con).prepareInsert();
  }

  /**
   * Convenience method for SQL INSERT statements that do not contain <i>Klojang
   * Templates</i> variables. Equivalent to:
   *
   * <blockquote><pre>{@code
   * SQL.simple(config, sql).session(con).prepareInsert();
   * }</pre></blockquote>
   *
   * @param con the JDBC connection to use
   * @param config a {@code SessionConfig} object that allows you to fine-tune how
   *       <i>Klojang JDBC</i> operates
   * @param retrieveKeys whether to retrieve the keys that were generated by the
   *       database. If the database does not generate any keys for the provided INSERT
   *       statement, you <b>must</b> specify {@code false}, or an exception will follow
   *       when executing the statement.
   * @param sql the SQL INSERT statement
   * @return a {@code SQLInsert} instance that allows you to bind the named parameters in
   *       the SQL (if present) and then execute it
   * @see SQLSession#prepareInsert(boolean)
   */
  static SQLInsert simpleInsert(Connection con,
        SessionConfig config,
        boolean retrieveKeys,
        String sql) {
    Check.notNull(con, CONNECTION);
    Check.notNull(config, CONFIG);
    Check.notNull(sql, SQL_ARGUMENT);
    return simple(config, sql).session(con).prepareInsert(retrieveKeys);
  }

  /**
   * Convenience method for SQL UPDATE statements that do not contain <i>Klojang
   * Templates</i> variables. Equivalent to:
   *
   * <blockquote><pre>{@code
   * SQL.simple(SessionConfig.getDefaultConfig(), sql).session(con).prepareUpdate();
   * }</pre></blockquote>
   *
   * @param con the JDBC connection to use
   * @param sql the SQL UPDATE statement
   * @return a {@code SQLUpdate} instance that allows you to bind the named parameters in
   *       the SQL (if present) and then execute it
   * @see SQLSession#prepareUpdate()
   */
  static SQLUpdate simpleUpdate(Connection con, String sql) {
    return simpleUpdate(con, DEFAULT_CONFIG, sql);
  }

  /**
   * Convenience method for SQL UPDATE statements that do not contain <i>Klojang
   * Templates</i> variables. Equivalent to:
   *
   * <blockquote><pre>{@code
   * SQL.simple(config, sql).session(con).prepareInsert();
   * }</pre></blockquote>
   *
   * @param con the JDBC connection to use
   * @param config a {@code SessionConfig} object that allows you to fine-tune how
   *       <i>Klojang JDBC</i> operates
   * @param sql the SQL UPDATE statement
   * @return a {@code SQLUpdate} instance that allows you to bind the named parameters in
   *       the SQL (if present) and then execute it
   * @see SQLSession#prepareUpdate()
   */
  static SQLUpdate simpleUpdate(Connection con, SessionConfig config, String sql) {
    Check.notNull(con, CONNECTION);
    Check.notNull(config, CONFIG);
    Check.notNull(sql, SQL_ARGUMENT);
    return simple(config, sql).session(con).prepareUpdate();
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
    return template(DEFAULT_CONFIG, sql);
  }

  /**
   * Returns a {@code SQL} implementation that allows for named parameters and
   * <i>Klojang Templates</i> variables. The SQL is read from the specified classpath
   * resource. The resulting {@code SQL} instance is cached and returned upon every
   * subsequent call with the same {@code clazz} and {@code sqlFile} arguments.
   *
   * @param clazz a {@code Class} object that provides access to the SQL file by
   *       calling {@code clazz.getResourceAsStream(sqlFile)}
   * @param sqlFile the location of the SQL file
   * @return a {@code SQL} implementation that allows for named parameters and
   *       <i>Klojang Templates</i> variables
   */
  static SQL template(Class<?> clazz, String sqlFile) {
    return SQLCache.get(clazz, sqlFile, SQL::template);
  }


  /**
   * Returns a {@code SQL} implementation that allows for named parameters and
   * <i>Klojang Templates</i> variables.
   *
   * @param config a {@code SessionConfig} object that allows you to fine-tune how
   *       <i>Klojang JDBC</i> operates
   * @param sql the SQL statement
   * @return a {@code SQL} implementation that allows for named parameters and
   *       <i>Klojang Templates</i> variables
   */
  static SQL template(SessionConfig config, String sql) {
    Check.notNull(config, CONFIG);
    Check.notNull(sql, SQL_ARGUMENT);
    return new SQLTemplate(sql, config);
  }

  /**
   * Returns a {@code SQL} implementation that allows for named parameters and
   * <i>Klojang Templates</i> variables. The SQL is read from the specified classpath
   * resource. The resulting {@code SQL} instance is cached and returned upon every
   * subsequent call with the same {@code clazz} and {@code sqlFile} arguments.
   *
   * @param config a {@code SessionConfig} object that allows you to fine-tune how
   *       <i>Klojang JDBC</i> operates
   * @param clazz a {@code Class} object that provides access to the SQL file by
   *       calling {@code clazz.getResourceAsStream(sqlFile)}
   * @param sqlFile the location of the SQL file
   * @return a {@code SQL} implementation that allows for named parameters and
   *       <i>Klojang Templates</i> variables
   */
  static SQL template(SessionConfig config, Class<?> clazz, String sqlFile) {
    return SQLCache.get(clazz, sqlFile, config, SQL::template);
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
    return skeleton(DEFAULT_CONFIG, sql);
  }


  /**
   * Returns a {@code SQL} implementation that allows for named parameters and
   * <i>Klojang Templates</i> variables. The template variables may be set to SQL
   * fragments that again contain named parameters. The SQL is read from the specified
   * classpath resource. The resulting {@code SQL} instance is cached and returned upon
   * every subsequent call with the same {@code clazz} and {@code sqlFile} arguments.
   *
   * @param clazz a {@code Class} object that provides access to the SQL file by
   *       calling {@code clazz.getResourceAsStream(sqlFile)}
   * @param sqlFile the location of the SQL file
   * @return a {@code SQL} implementation that allows for named parameters and
   *       <i>Klojang Templates</i> variables
   */
  static SQL skeleton(Class<?> clazz, String sqlFile) {
    return SQLCache.get(clazz, sqlFile, SQL::skeleton);
  }


  /**
   * Returns a {@code SQL} implementation that allows for named parameters and
   * <i>Klojang Templates</i> variables. The template variables may be set to SQL
   * fragments that again contain named parameters.
   *
   * @param config a {@code SessionConfig} object that allows you to fine-tune how
   *       <i>Klojang JDBC</i> operates
   * @param sql the SQL statement
   * @return a {@code SQL} implementation that allows for named parameters and
   *       <i>Klojang Templates</i> variables
   */
  static SQL skeleton(SessionConfig config, String sql) {
    Check.notNull(config, CONFIG);
    Check.notNull(sql, SQL_ARGUMENT);
    return new SQLSkeleton(sql, config);
  }

  /**
   * Returns a {@code SQL} implementation that allows for named parameters and
   * <i>Klojang Templates</i> variables. The template variables may be set to SQL
   * fragments that again contain named parameters. The SQL is read from the specified
   * classpath resource. The resulting {@code SQL} instance is cached and returned upon
   * every subsequent call with the same {@code clazz} and {@code sqlFile} arguments.
   *
   * @param config a {@code SessionConfig} object that allows you to fine-tune how
   *       <i>Klojang JDBC</i> operates
   * @param clazz a {@code Class} object that provides access to the SQL file by
   *       calling {@code clazz.getResourceAsStream(sqlFile)}
   * @param sqlFile the location of the SQL file
   * @return a {@code SQL} implementation that allows for named parameters and
   *       <i>Klojang Templates</i> variables
   */
  static SQL skeleton(SessionConfig config, Class<?> clazz, String sqlFile) {
    return SQLCache.get(clazz, sqlFile, config, SQL::skeleton);
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
  static BatchInsertBuilder insertBatch() {
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


}
