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
 * implementation can be obtained via the {@link #basic(String) SQL.basic()} method. You
 * should also preferably use this implementation for completely static SQL.
 *
 * <h2>SQL Templates</h2>
 * <p>A classic example of something that you often want to, but cannot parametrize with
 * JDBC are the column(s) in the ORDER BY clause. <i>Klojang JDBC</i> lets you do this
 * using <a
 * href="https://klojang4j.github.io/klojang-templates/1/api/org.klojang.templates/module-summary.html">Klojang
 * Template</a> variables. For example:
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
 * <p>An instance of this implementation can be obtained via the
 * {@link #template(String) SQL.template()} method. <i>Klojang JDBC</i> is agnostic about,
 * and has no opinion on what gets parametrized this way. You could also parametrize the
 * table(s) in the FROM clause for example.
 *
 * <h2>SQL Skeletons</h2>
 * <p>The implementation obtained via the {@link #skeleton(String) SQL.skeleton()}
 * methods allows for very dynamically generated SQL. As with SQL templates, the SQL is
 * provided in the form of a Klojang template (that is: it may contain <i>Klojang
 * Templates</i> variables). However, SQL skeletons explicitly allow you to set template
 * variables to values that are themselves chunks of SQL again. More importantly: if these
 * SQL chunks contain named parameters, they will be picked up by <i>Klojang JDBC</i>,
 * just like named parameters in the SQL skeleton. This makes SQL skeletons less
 * efficient, but more dynamic than SQL templates.
 *
 * @see org.klojang.templates.Template
 * @see org.klojang.templates.RenderSession
 */
public sealed interface SQL permits AbstractSQL {
  /**
   * Returns an {@code SQL} implementation that allows for named parameters, but not for
   * <i>Klojang Templates</i> variables.
   *
   * @param sql the SQL query string
   * @return an instance of an {@code SQL} implementation that behaves as described above
   */
  static SQL basic(String sql) {
    return basic(sql, new BindInfo() {});
  }

  /**
   * Returns an {@code SQL} implementation that allows for named parameters, but not for
   * <i>Klojang Templates</i> variables.
   *
   * @param sql the SQL query string
   * @param bindInfo a {@code BindInfo} object that allows you to fine-tune how values are
   * bound into the underlying {@link java.sql.PreparedStatement}
   * @return an instance of an {@code SQL} implementation that behaves as described above
   */
  static SQL basic(String sql, BindInfo bindInfo) {
    return new BasicSQL(sql, bindInfo);
  }

  /**
   * Returns an {@code SQL} implementation that allows for named parameters and
   * <i>Klojang Templates</i> variables.
   *
   * @param sql the SQL query string
   * @return an instance of an {@code SQL} implementation that behaves as described above
   */
  static SQL template(String sql) {
    return template(sql, new BindInfo() {});
  }

  /**
   * Returns an {@code SQL} implementation that allows for named parameters and
   * <i>Klojang Templates</i> variables.
   *
   * @param sql the SQL query string
   * @param bindInfo a {@code BindInfo} object that allows you to fine-tune how values are
   * bound into the underlying {@link java.sql.PreparedStatement}
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
    return skeleton(sql, new BindInfo() {});
  }

  /**
   * Returns an {@code SQL} implementation that allows for named parameters and
   * <i>Klojang Templates</i> variables. The template variables may be set to strings
   * that themselves again contain named parameters. These named parameters will be
   * detected and extracted by <i>Klojang JDBC</i>.
   *
   * @param sql the SQL query string
   * @param bindInfo a {@code BindInfo} object that allows you to fine-tune how values are
   * bound into the underlying {@link java.sql.PreparedStatement}
   * @return an instance of an {@code SQL} implementation that behaves as described above
   */
  static SQL skeleton(String sql, BindInfo bindInfo) {
    return new SQLSkeleton(sql, bindInfo);
  }

  /**
   * Returns an {@link SQLInsertBuilder} that enables you to easily configure an SQL
   * INSERT statement.
   *
   * @return an {@code SQLInsertBuilder} that enables you to easily configure an SQL
   * INSERT statement
   */
  static SQLInsertBuilder prepareInsert() {
    return new SQLInsertBuilder();
  }

  /**
   * Returns an {@link SQLBatchInsertBuilder} that enables you to easily configure large
   * batch inserts.
   *
   * @return an {@code SQLBatchInsertBuilder} that enables you to easily configure large
   * batch inserts.
   */
  static SQLBatchInsertBuilder prepareBatchInsert() {
    return new SQLBatchInsertBuilder();
  }

  /**
   * Returns a special object that signals to <i>Klojang JDBC</i> that the specified
   * string is to be treated as an SQL expression and hence must not be quoted or escaped.
   * You can use this when
   * {@link SQLBatchInsertBuilder#withTransformer specifying transformers} for beans to be
   * saved in a {@link SQLBatchInsert batch insert}. Note that this makes you responsible
   * for ensuring that the specified string does not and cannot suffer from SQL injection.
   * Use a {@link Quoter} to ensure that all strings in the expression are properly quoted
   * and escaped.
   *
   * @param expression the string to be wrapped into the signal object
   * @return a special object that signals to <i>Klojang JDBC</i> that the specified
   * string is to be treated as an SQL expression
   * @see java.sql.Statement#enquoteLiteral(String)
   * @see Quoter
   */
  static SQLExpression expression(String expression) {
    return new SQLExpression(expression);
  }

  /**
   * Returns a {@code SQLSession} that allows you to execute the SQL query.
   *
   * @return a {@code SQLSession} that allows you to execute the SQL query
   */
  SQLSession session(Connection con);

}
