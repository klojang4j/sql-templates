package org.klojang.jdbc;

import org.klojang.jdbc.x.sql.AbstractSQLSession;

import java.sql.Connection;

/**
 * An {@code SQLSession} is used to execute the SQL encapsulated in an {@link SQL} object.
 * Their main purpose is to allow the user to set SQL template variables.
 */
public sealed interface SQLSession permits AbstractSQLSession {

  /**
   * Sets the value of a <i>Klojang Templates</i> variable within a SQL template. This
   * method will throw an {@link UnsupportedOperationException} for
   * {@link SQL#parametrized(String) SQL.parametrized()} sessions since these are not
   * based on <i>Klojang Templates</i>.
   *
   * @param varName the name of the template variable
   * @param value the value to set the variable to. If the value is an array or
   *     collection, it will be "imploded" to a string, using {@code ", " } to separate
   *     the elements in the array or collection.
   * @return this {@code SQL} instance
   * @see org.klojang.templates.RenderSession#set(String, Object)
   */
  SQLSession set(String varName, Object value);

  /**
   * Sets a <i>Klojang Templates</i> variable name "sortColumn" to the specified value.
   * This presumes and requires that the SQL template indeed contains a variable with that
   * name. This is a convenience method facilitating the most common use case for
   * <i>Klojang Templates</i> variables: setting the column(s) in the ORDER BY clause
   *
   * @param sortColumn the column(s) to sort on
   * @return this {@code SQL} instance
   */
  default SQLSession setSortColumn(Object sortColumn) {
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
  default SQLSession setSortOrder(Object sortOrder) {
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
  default SQLSession setDescending(boolean isDescending) {
    return set("sortOrder", isDescending ? "DESC" : "ASC");
  }

  /**
   * Sets the components of an ORDER BY clause.
   *
   * @param sortColumn the column to sort on
   * @param sortOrder the sort order
   * @return this {@code SQL} instance
   */
  default SQLSession setOrderBy(Object sortColumn, Object sortOrder) {
    return setSortColumn(sortColumn).setSortOrder(sortOrder);
  }

  /**
   * Sets the components of an ORDER BY clause.
   *
   * @param sortColumn the column to sort on
   * @param isDescending whether to sort in descending order
   * @return this {@code SQL} instance
   */
  default SQLSession setOrderBy(Object sortColumn, boolean isDescending) {
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

  static SQLInsertBuilder prepareInsert() {
    return new SQLInsertBuilder();
  }
}
