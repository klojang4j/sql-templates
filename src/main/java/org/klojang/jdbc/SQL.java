package org.klojang.jdbc;

import org.klojang.jdbc.x.sql.AbstractSQL;
import org.klojang.jdbc.x.sql.BasicSQL;
import org.klojang.jdbc.x.sql.SQLSkeleton;
import org.klojang.jdbc.x.sql.SQLTemplate;

import java.sql.Connection;

/**
 * <p>Encapsulates a user-provided SQL query. The query string can be parametrized in
 * three qualitatively ways. For each of these variants a different implementation is
 * provided. The implementations are hidden from view. Instances of them are obtained via
 * static factory methods on the {@code SQL} interface itself.
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
 * implementation can be obtained via the {@link #basic(String) parametrized()} methods of
 * the {@code SQL} interface. You should also preferably use this implementation for
 * completely static SQL.
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
 * {@link #template(String) template()} methods of the {@code SQL} interface. <i>Klojang
 * JDBC</i> is agnostic about, and has no opinion on what gets parametrized this way. You
 * could also parametrize the table(s) in the FROM clause for example.
 *
 * <h2>SQL Skeletons</h2>
 * <p>The implementation obtained via the {@link #skeleton(String) skeleton()} methods
 * allows for very dynamically generated SQL. As with SQL templates, the SQL is provided
 * in the form of a Klojang template (that is: it may contain <i>Klojang Templates</i>
 * variables). However, SQL skeletons explicitly allow you to set template variables to
 * values that are themselves chunks of SQL. More importantly: these chunks of SQL may
 * again contain named parameters. Named parameters are only extracted <i>after</i> the
 * SQL template has been rendered. This makes SQL skeletons less efficient, but more
 * dynamic than SQL templates.
 *
 * <h2>SQL Injection</h2>
 * <p>As mentioned, named parameters are ultimately just placeholders for standard JDBC
 * parameters (question marks). Therefore, using them carries no risk of SQL injection.
 * This is not the case for SQL templates &#8212; that is, SQL containing
 * <i>Klojang Templates</i> variables. They can appear anywhere in a SQL template, and
 * <i>Klojang JDBC</i> does not make any attempt to parse, analyse or escape them.
 * Therefore, the values you provide for SQL template variables should preferably be
 * hard-coded somewhere in your own program. Accepting values of unknown origin is a bad
 * idea.
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
    return basic(sql, new BindInfo() { });
  }

  /**
   * Returns an {@code SQL} implementation that allows for named parameters, but not for
   * <i>Klojang Templates</i> variables.
   *
   * @param sql the SQL query string
   * @param bindInfo
   * @return an instance of an {@code SQL} implementation that behaves as described above
   */
  static SQL basic(String sql, BindInfo bindInfo) {
    return new BasicSQL(sql, bindInfo);
  }

  /**
   * Returns an {@code SQL} implementation that allows for named parameters and <i>Klojang
   * Templates</i> variables.
   *
   * @param sql the SQL query string
   * @return an instance of an {@code SQL} implementation that behaves as described above
   */
  static SQL template(String sql) {
    return template(sql, new BindInfo() { });
  }

  /**
   * Returns an {@code SQL} implementation that allows for named parameters and <i>Klojang
   * Templates</i> variables.
   *
   * @param sql the SQL query string
   * @param bindInfo
   * @return an instance of an {@code SQL} implementation that behaves as described above
   */
  static SQL template(String sql, BindInfo bindInfo) {
    return new SQLTemplate(sql, bindInfo);
  }

  /**
   * Returns an {@code SQL} implementation that allows for named parameters and <i>Klojang
   * Templates</i> variables. The template variables may be set to strings that themselves
   * again contain named parameters. These named parameters will be detected and extracted
   * by <i>Klojang JDBC</i>.
   *
   * @param sql the SQL query string
   * @return an instance of an {@code SQL} implementation that behaves as described above
   */
  static SQL skeleton(String sql) {
    return skeleton(sql, new BindInfo() { });
  }

  /**
   * Returns an {@code SQL} implementation that allows for named parameters and <i>Klojang
   * Templates</i> variables. The template variables may be set to strings that themselves
   * again contain named parameters. These named parameters will be detected and extracted
   * by <i>Klojang JDBC</i>.
   *
   * @param sql the SQL query string
   * @param bindInfo
   * @return an instance of an {@code SQL} implementation that behaves as described above
   */
  static SQL skeleton(String sql, BindInfo bindInfo) {
    return new SQLSkeleton(sql, bindInfo);
  }

  /**
   * Returns a {@code SQLQuery} instance that allows you to provide values for any named
   * parameters ("binding") within the SQL and then execute the query. The SQL is not
   * supposed to contain <i>Klojang Templates</i> variables. This method is a shortcut
   * for:
   * <blockquote><pre>{@code
   * basic(sql, bindInfo).session().prepareQuery(con)
   * }</pre></blockquote>
   *
   * @param con the JDBC connection to use for the query
   * @return a {@code SQLQuery} instance
   */
  static SQLQuery prepareQuery(Connection con, String sql, BindInfo bindInfo) {
    return basic(sql, bindInfo).session().prepareQuery(con);
  }

  /**
   * Returns a {@code SQLInsert} instance that allows you to provide values for any named
   * parameters ("binding") within the SQL and then execute the INSERT statement. The SQL
   * is not supposed to contain <i>Klojang Templates</i> variables. This method is a
   * shortcut for:
   * <blockquote><pre>{@code
   * basic(sql, bindInfo).session().prepareInsert(con)
   * }</pre></blockquote>
   *
   * @param con the JDBC connection to use for the query
   * @return a {@code SQLInsert} instance
   */
  static SQLInsert prepareInsert(Connection con, String sql, BindInfo bindInfo) {
    return basic(sql, bindInfo).session().prepareInsert(con);
  }

  /**
   * Returns a {@code SQLUpdate} instance that allows you to provide values for any named
   * parameters ("binding") within the SQL and then execute the UPDATE or DELETE
   * statement. The SQL is not supposed to contain <i>Klojang Templates</i> variables.
   * This method is a shortcut for:
   * <blockquote><pre>{@code
   * basic(sql, bindInfo).session().prepareUpdate(con)
   * }</pre></blockquote>
   *
   * @param con the JDBC connection to use for the query
   * @return a {@code SQLUpdate} instance
   */
  static SQLUpdate prepareUpdate(Connection con, String sql, BindInfo bindInfo) {
    return basic(sql, bindInfo).session().prepareUpdate(con);
  }

  /**
   * Returns an {@link SQLInsertBuilder} that enables you to easily configure an SQL
   * INSERT statement.
   *
   * @return an {@code SQLInsertBuilder} that enables you to easily configure an SQL
   *     INSERT statement
   */
  static SQLInsertBuilder prepareInsert() {
    return new SQLInsertBuilder();
  }

  /**
   * Returns a {@code SQLSession} that allows you to execute the SQL query.
   *
   * @return a {@code SQLSession} that allows you to execute the SQL query
   */
  SQLSession session();

}
