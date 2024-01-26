package org.klojang.jdbc;

import java.sql.Connection;

/**
 * <p>Encapsulates a user-provided SQL statement. The statement can be parametrized in
 * three qualitatively different ways. For each variant a different implementation of the
 * {@code SQL} interface is provided. The implementations are hidden from view. Instances
 * of them are obtained via static factory methods on the {@code SQL} interface itself.
 *
 * <h2>Named Parameters</h2>
 * <p>The simplest implementation supports the use of named parameters within SQL query
 * strings. For example:
 * <blockquote><pre>{@code
 * SELECT *
 *   FROM PERSON
 *  WHERE FIRST_NAME = :firstName
 *    AND LAST_NAME = :lastName
 *  LIMIT :from, :batchSize
 * }</pre></blockquote>
 * <p>Named parameters are placeholders for, and will ultimately be replaced by regular
 * JDBC parameters (that is: question marks). Thus, named parameters can and should be
 * used wherever you would ordinarily use regular JDBC parameters. An instance of this
 * implementation can be obtained via the {@link #simple(String) SQL.simple()} method. You
 * should also preferably use this implementation for completely static SQL.
 *
 * <h2>SQL Templates</h2>
 * <p>A classic example of something that you often want to, but cannot parametrize with
 * JDBC are the column(s) in the ORDER BY clause. <i>Klojang JDBC</i> lets you do this
 * using <b><a
 * href="https://klojang4j.github.io/klojang-templates/api/org.klojang.templates/module-summary.html">Klojang
 * Templates</a></b> variables. For example:
 * <blockquote><pre>{@code
 * SELECT *
 *   FROM PERSON
 *  WHERE FIRST_NAME = :firstName
 *    AND LAST_NAME = :lastName
 *  ORDER BY ~%sortColumn% ~%sortOrder%
 *  LIMIT :from, :batchSize
 * }</pre></blockquote>
 * <p>(Thus: named parameters look like this: <b>{@code :fooBar}</b>. <i>Klojang
 * Templates</i> variables look like this: <b>{@code ~%fooBar%}</b>.)
 *
 * <p><i>Klojang Templates</i> variables can be set in the {@link SQLSession} that you
 * obtain from the {@code SQL} instance. Named parameters can be set (a.k.a. "bound") in
 * the {@link SQLStatement} that you again obtain from the {@code SQLSession}. Since
 * "simple" SQL is not allowed to contain <i>Klojang Templates</i> variables, the
 * {@code SQL} interface contains a few convenience methods that yield an
 * {@link SQLStatement} instance straight away, even though under the hood they still
 * create a {@link SQLSession} object.
 *
 *
 * <p>SQL templates can be created using the {@link #template(String) SQL.template()}
 * method. <i>Klojang JDBC</i> is agnostic about, and has no opinion on what gets
 * parametrized this way. You could also parametrize the table(s) in the FROM clause for
 * example.
 *
 * <h2>SQL Skeletons</h2>
 * <p>The implementation obtained via the {@link #skeleton(String) SQL.skeleton()}
 * methods allows for very dynamically generated SQL. As with SQL templates, the SQL is
 * provided in the form of a Klojang template (that is: it may contain <i>Klojang
 * Templates</i> variables). However, SQL skeletons explicitly allow you to set template
 * variables to values that are themselves chunks of SQL again. If the SQL chunks contain
 * named parameters, <i>Klojang JDBC</i> will register them, and you can provide values
 * for them just like you can for the named parameters in the SQL skeleton. This is not
 * possible with the implementation returned by {@code SQL.template()}. This makes SQL
 * skeletons less efficient, but more dynamic than SQL templates.
 *
 * @see org.klojang.templates.Template
 * @see org.klojang.templates.RenderSession
 */
public sealed interface SQL permits AbstractSQL {
  /**
   * Returns an {@code SQL} implementation that allows for named parameters, but not for
   * <i>Klojang Templates</i> variables. Also use this implementation for completely
   * static SQL.
   *
   * @param sql the SQL query string
   * @return an instance of an {@code SQL} implementation that behaves as described above
   */
  static SQL simple(String sql) {
    return simple(sql, new BindInfo() { });
  }

  /**
   * Returns an {@code SQL} implementation that allows for named parameters, but not for
   * <i>Klojang Templates</i> variables. Also use this implementation for completely
   * static SQL.
   *
   * @param sql the SQL query string
   * @param bindInfo a {@code BindInfo} object that allows you to fine-tune how
   *       values are bound into the underlying {@link java.sql.PreparedStatement}
   * @return an instance of an {@code SQL} implementation that behaves as described above
   */
  static SQL simple(String sql, BindInfo bindInfo) {
    return new BasicSQL(sql, bindInfo);
  }

  /**
   * Convenience method for SQL SELECT statements that do not contain <i>Klojang
   * Templates</i> variables. Equivalent to:
   *
   * <blockquote><pre>{@code
   * SQL.simple(sql, new BindInfo() {}).session(con).prepareQuery();
   * }</pre></blockquote>
   *
   * @param con the JDBC connection to use
   * @param sql the SQL SELECT string
   * @return an {@code SQLQuery} instance that allows you to bind the named parameters in
   *       the SQL (if present) and then execute it
   * @see SQLSession#prepareQuery()
   */
  static SQLQuery simpleQuery(Connection con, String sql) {
    return simpleQuery(con, sql, noBindInfo());
  }

  /**
   * Convenience method for SQL SELECT statements that do not contain <i>Klojang
   * Templates</i> variables. Equivalent to:
   *
   * <blockquote><pre>{@code
   * SQL.simple(sql, bindInfo).session(con).prepareQuery();
   * }</pre></blockquote>
   *
   * @param con the JDBC connection to use
   * @param sql the SQL SELECT string
   * @param bindInfo a {@code BindInfo} object that allows you to fine-tune how
   *       values are bound into the underlying {@link java.sql.PreparedStatement}
   * @return an {@code SQLQuery} instance that allows you to bind the named parameters in
   *       the SQL (if present) and then execute it
   * @see SQLSession#prepareQuery()
   */
  static SQLQuery simpleQuery(Connection con, String sql, BindInfo bindInfo) {
    return simple(sql, bindInfo).session(con).prepareQuery();
  }

  /**
   * Convenience method for SQL INSERT statements that do not contain <i>Klojang
   * Templates</i> variables. Equivalent to:
   *
   * <blockquote><pre>{@code
   * SQL.simple(sql, new BindInfo() {}).session(con).prepareInsert();
   * }</pre></blockquote>
   *
   * @param con the JDBC connection to use
   * @param sql the SQL INSERT string
   * @return an {@code SQLInsert} instance that allows you to bind the named parameters in
   *       the SQL (if present) and then execute it
   * @see SQLSession#prepareInsert()
   */
  static SQLInsert simpleInsert(Connection con, String sql) {
    return simple(sql, noBindInfo()).session(con).prepareInsert();
  }

  /**
   * Convenience method for SQL INSERT statements that do not contain <i>Klojang
   * Templates</i> variables. Equivalent to:
   *
   * <blockquote><pre>{@code
   * SQL.simple(sql, bindInfo).session(con).prepareInsert();
   * }</pre></blockquote>
   *
   * @param con the JDBC connection to use
   * @param sql the SQL INSERT string
   * @param retrieveKeys whether to retrieve the keys that were generated by the
   *       database
   * @param bindInfo a {@code BindInfo} object that allows you to fine-tune how
   *       values are bound into the underlying {@link java.sql.PreparedStatement}
   * @return an {@code SQLInsert} instance that allows you to bind the named parameters in
   *       the SQL (if present) and then execute it
   * @see SQLSession#prepareInsert(boolean)
   */
  static SQLInsert simpleInsert(Connection con,
        String sql,
        boolean retrieveKeys,
        BindInfo bindInfo) {
    return simple(sql, bindInfo).session(con).prepareInsert(retrieveKeys);
  }

  /**
   * Convenience method for SQL UPDATE statements that do not contain <i>Klojang
   * Templates</i> variables. Equivalent to:
   *
   * <blockquote><pre>{@code
   * SQL.simple(sql, new BindInfo() {}).session(con).prepareUpdate();
   * }</pre></blockquote>
   *
   * @param con the JDBC connection to use
   * @param sql the SQL UPDATE string
   * @return an {@code SQLUpdate} instance that allows you to bind the named parameters in
   *       the SQL (if present) and then execute it
   * @see SQLSession#prepareUpdate()
   */
  static SQLUpdate simpleUpdate(Connection con, String sql) {
    return simpleUpdate(con, sql, noBindInfo());
  }

  /**
   * Convenience method for SQL UPDATE statements that do not contain <i>Klojang
   * Templates</i> variables. Equivalent to:
   *
   * <blockquote><pre>{@code
   * SQL.simple(sql, bindInfo).session(con).prepareUpdate();
   * }</pre></blockquote>
   *
   * @param con the JDBC connection to use
   * @param sql the SQL UPDATE string
   * @param bindInfo a {@code BindInfo} object that allows you to fine-tune how
   *       values are bound into the underlying {@link java.sql.PreparedStatement}
   * @return an {@code SQLUpdate} instance that allows you to bind the named parameters in
   *       the SQL (if present) and then execute it
   * @see SQLSession#prepareUpdate()
   */
  static SQLUpdate simpleUpdate(Connection con, String sql, BindInfo bindInfo) {
    return simple(sql, bindInfo).session(con).prepareUpdate();
  }


  private static BindInfo noBindInfo() {
    return new BindInfo() { };
  }

  /**
   * Returns an {@code SQL} implementation that allows for named parameters and
   * <i>Klojang Templates</i> variables.
   *
   * @param sql the SQL query string
   * @return an instance of an {@code SQL} implementation that behaves as described above
   */
  static SQL template(String sql) {
    return template(sql, new BindInfo() { });
  }

  /**
   * Returns an {@code SQL} implementation that allows for named parameters and
   * <i>Klojang Templates</i> variables.
   *
   * @param sql the SQL query string
   * @param bindInfo a {@code BindInfo} object that allows you to fine-tune how
   *       values are bound into the underlying {@link java.sql.PreparedStatement}
   * @return an instance of an {@code SQL} implementation that behaves as described above
   */
  static SQL template(String sql, BindInfo bindInfo) {
    return new SQLTemplate(sql, bindInfo);
  }

  /**
   * Returns an {@code SQL} implementation that allows for named parameters and
   * <i>Klojang Templates</i> variables. The template variables may be set to strings
   * that themselves again contain named parameters. These named parameters will be
   * detected and extracted by <i>Klojang JDBC</i>.
   *
   * @param sql the SQL query string
   * @return an instance of an {@code SQL} implementation that behaves as described above
   */
  static SQL skeleton(String sql) {
    return skeleton(sql, new BindInfo() { });
  }


  /**
   * Returns an {@code SQL} implementation that allows for named parameters and
   * <i>Klojang Templates</i> variables. The template variables may be set to strings
   * that again contain named parameters. These named parameters will be detected and
   * extracted by <i>Klojang JDBC</i>, and you can bind them in the {@link SQLStatement}
   * derived from the {@code SQL} implementation.
   *
   * @param sql the SQL query string
   * @param bindInfo a {@code BindInfo} object that allows you to fine-tune how
   *       values are bound into the underlying {@link java.sql.PreparedStatement}
   * @return an instance of an {@code SQL} implementation that behaves as described above
   */
  static SQL skeleton(String sql, BindInfo bindInfo) {
    return new SQLSkeleton(sql, bindInfo);
  }

  /**
   * Returns an {@link InsertBuilder} that enables you to easily configure an SQL INSERT
   * statement.
   *
   * @return an {@code SQLInsertBuilder} that enables you to easily configure an SQL
   *       INSERT statement
   */
  static InsertBuilder configureInsert() {
    return new InsertBuilder();
  }

  /**
   * Returns an {@link BatchInsertBuilder} that enables you to easily configure large
   * batch inserts.
   *
   * @return an {@code SQLBatchInsertBuilder} that enables you to easily configure large
   *       batch inserts.
   */
  static BatchInsertBuilder configureBatchInsert() {
    return new BatchInsertBuilder();
  }

  /**
   * Returns a special object that signals to <i>Klojang JDBC</i> that the specified
   * string is to be treated as an SQL expression and hence must not be quoted or escaped.
   * You can use this when
   * {@link BatchInsertBuilder#withTransformer specifying transformers} for beans to be
   * saved in a {@link SQLBatchInsert batch insert}. Note that this makes you responsible
   * for ensuring that the specified string does not and cannot suffer from SQL injection.
   * Use a {@link Quoter} to ensure that all strings in the expression are properly quoted
   * and escaped.
   *
   * @param expression the string to be wrapped into the signal object
   * @return a special object that signals to <i>Klojang JDBC</i> that the specified
   *       string is to be treated as an SQL expression
   * @see java.sql.Statement#enquoteLiteral(String)
   * @see Quoter
   */
  static SQLExpression expression(String expression) {
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
