package org.klojang.jdbc;

import java.util.Collection;

/**
 * <p>An {@code SQLSession} is used to initiate and prepare the execution of SQL.
 * It allows you to set SQL <i>template variables</i> within the SQL and then obtain a
 * {@link SQLStatement} object that can be used to set (a.k.a. "bind") the <i>named
 * parameters</i> within the SQL. The difference between template variables and named
 * parameters is explained in the comments for the {@link SQL} interface. The
 * {@code SQLSession} implementation you get from {@link SQL#basic(String) SQL.basic()})
 * does not support template variables. This leaves you no choice but to retrieve a
 * {@code SQLStatement} from it straight away.
 *
 * <p>Probably the most common use case for using template variables is to parametrize
 * the ORDER BY column and sort order ("ASC" or "DESC"). Therefore the {@code SQLSession}
 * contains a few specialized {@code set} methods specifically for this purpose. These
 * methods assume that the SQL template contains a template variable named "sortColumn"
 * for the ORDER BY column(s), and a template variable name "sortOrder" for the sort
 * order.
 *
 * <p>The difference between the {@code SQLSession} you get from
 * {@link SQL#template(String) SQL.template()} and the one you get from
 * {@link SQL#skeleton(String) SQL.skeleton()} is that with the latter, named parameters
 * are extracted from the SQL at the very last moment, just before you retrieve a
 * {@link SQLStatement} from the session. Thus, if the SQL contained template variables,
 * and you set one or more of them to text values that again contain named parameters,
 * these, too, will be available for binding in the {@code SQLStatement}.
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
   * Sets the value of the specified template variable. If the value is an array or
   * collection, it will be "imploded" to a string, using {@code "," } (comma) to separate
   * the elements in the array or collection. This method will throw an
   * {@link UnsupportedOperationException} for
   * {@linkplain SQL#basic(String) basic SQL sessions} since these are not based on <a
   * href="https://klojang4j.github.io/klojang-templates/1/api/org.klojang.templates/module-summary.html">Klojang
   * Templates</a>.
   *
   * @param varName the name of the template variable
   * @param value the value to set the variable to. If the value is an array or
   *       collection, it will be "imploded" to a string, using {@code "," } (comma) to
   *       separate the elements in the array or collection.
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#basic(String) SQL.basic()} method
   * @see org.klojang.templates.Template
   * @see org.klojang.templates.RenderSession#set(String, Object)
   * @see org.klojang.util.ArrayMethods#implode(Object[])
   * @see org.klojang.util.CollectionMethods#implode(Collection)
   */
  default SQLSession set(String varName, Object value)
        throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }


  /**
   * Sets the value of the specified template variable. Use this method if the source of
   * the value is unknown to prevent SQL injection. The provided value is processed as
   * follows:
   * <ol>
   *   <li>If the value is {@code null}, the literal string "NULL" (without quotes)
   *   is inserted into the SQL template.
   *   <li>If the value is a {@link Number} or {@link Boolean}, it is inserted as-is
   *   (unquoted) into the SQL template.
   *   <li>If the value is an {@link SQLExpression}, like
   *   {@code SUBSTRING(FIRST_NAME, 1, 4)}, it is inserted as-is (unquoted) into
   *   the SQL template.
   *   <li>Otherwise the value is first escaped and quoted according to the underlying
   *   database's quoting rules and then inserted into the SQL template.
   *   <li>However, if the value is a collection or array, each of the elements is treated
   *   as described in the first four rules, and they are then stringed together using
   *   a comma (",") to separate them. The result of these two steps is then inserted into
   *   the SQL template.
   * </ol>
   *
   * @param varName the name of the template variable
   * @param value the value to set the variable to
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#basic(String) SQL.basic()} method
   * @see java.sql.Statement#enquoteLiteral(String)
   */
  default SQLSession setAsLiteral(String varName, Object value)
        throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  default SQLSession setAsIdentifier(String varName, String identifier)
        throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }


  /**
   * Sets a template variable named "sortColumn" to the specified value. This presumes and
   * requires that the SQL template indeed contains a variable with that name. This is a
   * convenience method facilitating the most common use case for template variables: to
   * parametrize the column(s) in the ORDER BY clause.
   *
   * @param sortColumn the column(s) to sort on
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#basic(String) SQL.basic()} method
   */
  default SQLSession setOrderBy(Object sortColumn) throws UnsupportedOperationException {
    return set("sortColumn", sortColumn);
  }

  /**
   * Sets a template variable named "sortOrder" to the specified value. This presumes and
   * requires that the SQL template indeed contains a variable with that name. The
   * provided value supposedly is either "ASC" or "DESC". The value may also be a boolean,
   * in which case {@code true} is translated into "DESC" and {@code false} into "ASC".
   * This is a convenience method facilitating the most common use case for template
   * variables: to parametrize the sort order for the column(s) in the ORDER BY clause.
   *
   * @param sortOrder the sort order
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#basic(String) SQL.basic()} method
   */
  default SQLSession setSortOrder(Object sortOrder) throws UnsupportedOperationException {
    return (sortOrder instanceof Boolean)
          ? setDescending((Boolean) sortOrder)
          : set("sortOrder", sortOrder);
  }

  /**
   * Sets a template variable named "sortOrder" to "DESC" if the argument equals
   * {@code true}, else to "ASC".
   *
   * @param isDescending whether to sort in descending order
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#basic(String) SQL.basic()} method
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
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#basic(String) SQL.basic()} method
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
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#basic(String) SQL.basic()} method
   */
  default SQLSession setOrderBy(Object sortColumn, boolean isDescending)
        throws UnsupportedOperationException {
    return setOrderBy(sortColumn).setDescending(isDescending);
  }

  /**
   * Returns a {@code SQLQuery} instance that allows you to provide values for named
   * parameters ("binding") and then execute the query.
   *
   * @return a {@code SQLQuery} instance
   */
  SQLQuery prepareQuery();

  /**
   * Returns a {@code SQLInsert} instance that allows you to provide values for named
   * parameters ("binding") and then execute the INSERT statement. Auto-generated keys
   * will be made available to the client.
   *
   * @return a {@code SQLInsert} instance
   */
  default SQLInsert prepareInsert() {
    return prepareInsert(true);
  }

  /**
   * Returns a {@code SQLInsert} instance that allows you to provide values for named
   * parameters ("binding") and then execute the INSERT statement.
   *
   * @param retrieveAutoKeys whether to retrieve the keys that were generated by the
   *       database
   * @return a {@code SQLInsert} instance
   */
  SQLInsert prepareInsert(boolean retrieveAutoKeys);

  /**
   * Returns a {@code SQLUpdate} instance that allows you to provide values for named
   * parameters ("binding") and then execute the UPDATE or DELETE statement.
   *
   * @return a {@code SQLUpdate} instance
   */
  SQLUpdate prepareUpdate();

}
