package org.klojang.jdbc;

import org.klojang.jdbc.x.sql.AbstractSQLSession;

import java.sql.Connection;

/**
 * <p>An {@code SQLSession} is used to initiate and partly prepare the execution of SQL.
 * It allows the user to set SQL <i>template variables</i> within the SQL and then obtain
 * a {@link SQLStatement} object that can be used to set (a.k.a. "bind") the <i>named
 * parameters</i> within the SQL. The difference between template variables and named
 * parameters is explained in the comments for the {@link SQL} interface. Note that one of
 * the implementations of {@code SQLSession} (the one you get from
 * {@link SQL#basic(String) SQL.basic()}) does not support template variables and
 * therefore calls to its {@link #set(String, Object) set()} method will throw an
 * {@link UnsupportedOperationException}.
 *
 * <p>Probably the most common use case for using template variables is to parametrize
 * the ORDER BY column and sort order ("ASC" or "DESC"). Therefore the {@code SQLSession}
 * contains a few specialized {@code set} methods specifically for this purpose. These
 * methods assume that the SQL template contains a template variable named "sortColumn"
 * for the ORDER BY column(s), and a template variable name "sortOrder" for the sort
 * order.
 *
 * <p><i>An {@code SQLSession} is not thread-safe and should generally not be reused once
 * you have obtained a {@code SQLStatement} object from it.</i>
 */

/*
 * In fact all implementations currently _are_ thread-safe, but they are not intended to
 * be, and we don't want to commit to it.
 */
public sealed interface SQLSession permits AbstractSQLSession {

  /**
   * Sets the value of a template variable. This method will throw an
   * {@link UnsupportedOperationException} for {@link SQL#basic(String) SQL.basic()}
   * sessions since these are not based on <a
   * href="https://klojang4j.github.io/klojang-templates/1/api/org.klojang.templates/module-summary.html">Klojang
   * Template</a>.
   *
   * @param varName the name of the template variable
   * @param value the value to set the variable to. If the value is an array or
   *     collection, it will be "imploded" to a string, using {@code ", " } to separate
   *     the elements in the array or collection.
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *     obtained via the {@link SQL#basic(String) basic()} methods of the {@link SQL}
   *     class.
   * @see org.klojang.templates.RenderSession#set(String, Object)
   */
  SQLSession set(String varName, Object value) throws UnsupportedOperationException;

  /**
   * Sets a template variable name "sortColumn" to the specified value. This presumes and
   * requires that the SQL template indeed contains a variable with that name. This is a
   * convenience method facilitating the most common use case for template variables: to
   * parametrize the column(s) in the ORDER BY clause.
   *
   * @param sortColumn the column(s) to sort on
   * @return this {@code SQLSession} instance
   */
  default SQLSession setOrderBy(Object sortColumn)
      throws UnsupportedOperationException {
    return set("sortColumn", sortColumn);
  }

  /**
   * Sets a template variable name "sortOrder" to the specified value. This presumes and
   * requires that the SQL template indeed contains a variable with that name. The
   * provided value supposedly is either "ASC" or "DESC". The value may also be a boolean,
   * in which case {@code true} is translated into "DESC" and {@code false} into "ASC".
   * This is a convenience method facilitating the most common use case for template
   * variables: to parametrize the sort order for the column(s) in the ORDER BY clause.
   *
   * @param sortOrder the sort order
   * @return this {@code SQLSession} instance
   */
  default SQLSession setSortOrder(Object sortOrder)
      throws UnsupportedOperationException {
    return (sortOrder instanceof Boolean)
        ? setDescending((Boolean) sortOrder)
        : set("sortOrder", sortOrder);
  }

  /**
   * Sets a template variable name "sortOrder" to "DESC" if the argument equals
   * {@code true}, else to "ASC".
   *
   * @param isDescending whether to sort in descending order
   * @return this {@code SQLSession} instance
   */
  default SQLSession setDescending(boolean isDescending)
      throws UnsupportedOperationException {
    return set("sortOrder", isDescending ? "DESC" : "ASC");
  }

  /**
   * Sets the sort column and sort order of an ORDER BY clause.
   *
   * @param sortColumn the column to sort on
   * @param sortOrder the sort order
   * @return this {@code SQLSession} instance
   */
  default SQLSession setOrderBy(Object sortColumn, Object sortOrder)
      throws UnsupportedOperationException {
    return setOrderBy(sortColumn).setSortOrder(sortOrder);
  }

  /**
   * Sets the ort column and sort order of an ORDER BY clause.
   *
   * @param sortColumn the column to sort on
   * @param isDescending whether to sort in descending order
   * @return this {@code SQLSession} instance
   */
  default SQLSession setOrderBy(Object sortColumn, boolean isDescending)
      throws UnsupportedOperationException {
    return setOrderBy(sortColumn).setDescending(isDescending);
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
