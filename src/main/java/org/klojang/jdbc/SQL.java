package org.klojang.jdbc;

import org.klojang.jdbc.x.sql.AbstractSQL;
import org.klojang.jdbc.x.sql.ParametrizedSQL;
import org.klojang.jdbc.x.sql.SQLSkeleton;
import org.klojang.jdbc.x.sql.SQLTemplate;

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
     * @param sql      the SQL query string
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
     * @param sql      the SQL query string
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
     * @param sql      the SQL query string
     * @param bindInfo
     * @return an instance of an {@code SQL} implementation that behaves as described above
     */
    static SQL skeleton(String sql, BindInfo bindInfo) {
        return new SQLSkeleton(sql, bindInfo);
    }

    SQLSession newSession();

}
