package org.klojang.jdbc;

import org.klojang.jdbc.x.sql.AbstractSQL;
import org.klojang.jdbc.x.sql.ParametrizedSQL;
import org.klojang.jdbc.x.sql.SQLTemplate;
import org.klojang.jdbc.x.sql.SkeletonSQL;

import java.sql.Connection;

/**
 * <p>Encapsulates a user-provided SQL query string. The query string can be parametrized
 * in three qualitatively ways. For each of these variants a different implementation is
 * provided. These implementations are hidden from view. Instances of them are obtained
 * via static factory methods on the {@code SQL} interface itself.
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
 * <p>Named parameters can and should be used everywhere you would ordinarily use
 * standard JDBC parameters (that is: question marks). An instance of this implementation
 * can be obtained via {@link #parametrized(String) SQL.parametrized(sql)}. You should
 * also preferably use this implementation for completely static SQL.
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
 * <p>(Thus: named parameters look like this: <b>{@code :foo}</b>. <i>Klojang
 * Templates</i> variables look like this: <b>{@code ~%foo%}</b>.)
 *
 * <p>An instance of this implementation can be obtained via {@link #template(String)}
 * SQL.template(sql)}. <i>Klojang JDBC</i> is agnostic and has no opinion on what gets
 * parametrized this way. You could also parametrize the table(s) in the FROM clause for
 * example.
 *
 * <h2>SQL Skeletons</h2>
 * <p>The implementation obtained via {@link #skeleton(String) SQL.skeleton(sql)} allows
 * for very dynamically generated SQL. As with SQL templates, the SQL is provided in the
 * form of a <i>Klojang Template</i>. However, SQL skeletons explicitly allow you to set
 * template variables to values that are themselves chunks of SQL. More importantly: these
 * chunks of SQL may again contain named parameters. Named parameters are only extracted
 * <i>after</i> the SQL template has been rendered by <i>Klojang Templates</i>. This
 * makes SQL skeletons less efficient, but more dynamic than SQL templates.
 *
 * @see org.klojang.templates.Template
 */
public sealed interface SQL permits AbstractSQL {

  /**
   * Returns an {@code SQL} implementation that allows for named parameters, but not for
   * <i>Klojang Templates</i> variables.
   *
   * @param sql the SQL query string
   * @return an instance of an {@code SQL} implementation that behaves as described above
   */
  static SQL parametrized(String sql) {
    return parametrized(sql, new BindInfo() {});
  }

  /**
   * Returns an {@code SQL} implementation that allows for named parameters, but not for
   * <i>Klojang Templates</i> variables.
   *
   * @param sql the SQL query string
   * @param bindInfo
   * @return an instance of an {@code SQL} implementation that behaves as described above
   */
  static SQL parametrized(String sql, BindInfo bindInfo) {
    return new ParametrizedSQL(sql, bindInfo);
  }

  /**
   * Returns an {@code SQL} implementation that allows for named parameters and <i>Klojang
   * Templates</i> variables.
   *
   * @param sql the SQL query string
   * @return an instance of an {@code SQL} implementation that behaves as described above
   */
  static SQL template(String sql) {
    return template(sql, new BindInfo() {});
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
   * again contain named parameters. These named parameters, too, will be detected and
   * extracted by <i>Klojang JDBC</i>.
   *
   * @param sql the SQL query string
   * @return an instance of an {@code SQL} implementation that behaves as described above
   */
  static SQL skeleton(String sql) {
    return skeleton(sql, new BindInfo() {});
  }

  /**
   * Returns an {@code SQL} implementation that allows for named parameters and <i>Klojang
   * Templates</i> variables. The template variables may be set to strings that themselves
   * again contain named parameters. These named parameters, too, will be detected and
   * extracted by <i>Klojang JDBC</i>.
   *
   * @param sql the SQL query string
   * @param bindInfo
   * @return an instance of an {@code SQL} implementation that behaves as described above
   */
  static SQL skeleton(String sql, BindInfo bindInfo) {
    return new SkeletonSQL(sql, bindInfo);
  }

  static SQLInsertBuilder prepareInsert() {
    return new SQLInsertBuilder();
  }

  /**
   * Sets the value of a <i>Klojang Templates</i> variable within a SQL template. This
   * method will throw an {@link UnsupportedOperationException} for the {@code SQL}
   * implementation returned by the {@link #parametrized(String) parametrized()} methods
   * since that implementation is not based on <i>Klojang Templates</i>.
   *
   * @param varName the name of the template variable
   * @param value the value to set the variable to. If the value is an array or
   * collection, it will be "imploded" to a string, using {@code ", " } to separate the
   * elements in the array or collection.
   * @return this {@code SQL} instance
   * @see org.klojang.templates.RenderSession#set(String, Object)
   */
  SQL set(String varName, Object value);

  /**
   * Sets a <i>Klojang Templates</i> variable name "sortColumn" to the specified value.
   * This presumes and requires that the SQL template indeed contains a variable with that
   * name. This is a convenience method facilitating the most common use case for
   * <i>Klojang Templates</i> variables: setting the column(s) in the ORDER BY clause
   *
   * @param sortColumn the column(s) to sort on
   * @return this {@code SQL} instance
   */
  default SQL setSortColumn(Object sortColumn) {
    return set("sortColumn", sortColumn);
  }

  /**
   * Sets a <i>Klojang Templates</i> variable name "sortOrder" to the specified value.
   * This presumes and requires that the SQL template indeed contains a variable with that
   * name. The provided value supposedly is either "ASC" or "DESC". The value may also be
   * a boolean, in which case {@code true} is translated into "DESC" and {@code false}
   * into "ASC". This is a convenience method facilitating the most common use case for
   * <i>Klojang Templates</i> variables: setting the sort order for the column(s) in the
   * ORDER BY clause.
   *
   * @param sortOrder the sort order
   * @return this {@code SQL} instance
   */
  default SQL setSortOrder(Object sortOrder) {
    return (sortOrder instanceof Boolean)
          ? setDescending((Boolean) sortOrder)
          : set("sortOrder", sortOrder);
  }

  /**
   * Sets a <i>Klojang Templates</i> variable name "sortOrder" to "DESC" if the argument
   * equals {@code true}, else to "ASC".
   *
   * @param isDescending whether to sort in descending order
   * @return this {@code SQL} instance
   */
  default SQL setDescending(boolean isDescending) {
    return set("sortOrder", isDescending ? "DESC" : "ASC");
  }

  /**
   * Sets the components of an ORDER BY clause.
   *
   * @param sortColumn the column to sort on
   * @param sortOrder the sort order
   * @return this {@code SQL} instance
   */
  default SQL setOrderBy(Object sortColumn, Object sortOrder) {
    return setSortColumn(sortColumn).setSortOrder(sortOrder);
  }

  /**
   * Sets the components of an ORDER BY clause.
   *
   * @param sortColumn the column to sort on
   * @param isDescending whether to sort in descending order
   * @return this {@code SQL} instance
   */
  default SQL setOrderBy(Object sortColumn, boolean isDescending) {
    return setSortColumn(sortColumn).setDescending(isDescending);
  }

  /**
   * Returns a {@code SQLQuery} instance that allows you to provide values for named
   * parameters ("binding") and then execute the query.
   *
   * @param con the JDBC connection to use for the query
   * @return a {@code SQLQuery} instance
   */
  SQLQuery prepareQuery(Connection con);

  /**
   * Returns a {@code SQLInsert} instance that allows you to provide values for named
   * parameters ("binding") and then execute the INSERT statement.
   *
   * @param con the JDBC connection to use for the query
   * @return a {@code SQLInsert} instance
   */
  SQLInsert prepareInsert(Connection con);

  /**
   * Returns a {@code SQLUpdate} instance that allows you to provide values for named
   * parameters ("binding") and then execute the UPDATE or DELETE statement.
   *
   * @param con the JDBC connection to use for the query
   * @return a {@code SQLUpdate} instance
   */
  SQLUpdate prepareUpdate(Connection con);
}
