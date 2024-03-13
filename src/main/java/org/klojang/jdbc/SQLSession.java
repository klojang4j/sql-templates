package org.klojang.jdbc;

import java.util.Collection;

/**
 * <p>A {@code SQLSession} allows you to provide values for <i>template variables</i>
 * within an SQL template. Once you have set all template variables, you can obtain a
 * {@link SQLStatement} from the {@code SQLSession} and use it to set (a.k.a. "bind") the
 * <i>named parameters</i> within the SQL. The difference between template variables and
 * named parameters is explained in the comments for the {@linkplain SQL SQL interface}.
 * Since the {@code SQLSession} implementation obtained via
 * {@link SQL#simple(String) SQL.simple()}) does not allow for template variables, you
 * have no choice but to retrieve a {@code SQLStatement} from it straight away. If the SQL
 * does not contain any named parameters, you may also call {@link #execute()} on the
 * {@code SQLSession}, without going through the creation of a {@code SQLStatement}.
 *
 * <p>{@code SQLSession} instances are meant to be throw-away objects that should, in
 * principle, not survive the method in which they are created.
 */
public sealed interface SQLSession permits AbstractSQLSession {

  private static UnsupportedOperationException notSupported(String method) {
    String fmt = "method %s() only supported for SQL templates and SQL skeletons";
    return new UnsupportedOperationException(String.format(fmt, method));
  }

  private static UnsupportedOperationException sqlSkeletonsOnly(String method) {
    String fmt = "method %s() only supported for SQL skeletons";
    return new UnsupportedOperationException(String.format(fmt, method));
  }

  /**
   * Sets the specified template variable to the specified value. Only use this method if
   * you know and trust the origin of the provided value. The value will not be escaped or
   * quoted. Preferably use {@link #setValue(String, Object) setValue()} or
   * {@link #setIdentifier(String, String) setIdentifier()} to prevent SQL injection. If
   * the value is an array or collection, it will be "imploded" to a string, using
   * {@code "," } (comma) to separate the elements in the array or collection. This method
   * will throw an {@link UnsupportedOperationException} for
   * {@linkplain SQL#simple(String) simple SQL sessions} since these are not based on <a
   * href="https://klojang4j.github.io/klojang-templates/1/api/org.klojang.templates/module-summary.html">Klojang
   * Templates</a>.
   *
   * @param varName the name of the template variable
   * @param value the value to set the variable to
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#simple(String) SQL.simple()} method
   * @see org.klojang.templates.Template
   * @see org.klojang.templates.RenderSession#set(String, Object)
   * @see org.klojang.util.ArrayMethods#implode(Object[])
   * @see org.klojang.util.CollectionMethods#implode(Collection)
   */
  default SQLSession set(String varName, Object value)
        throws UnsupportedOperationException {
    throw notSupported("set");
  }

  /**
   * Sets the specified template variable to the escaped and quoted version of the
   * specified value. Use this method if you do not know or trust the origin of the value
   * to prevent SQL injection. If the value is an array or {@link Collection}, it will be
   * "imploded" to a string, using a comma to separate the elements in the array or
   * collection, and using {@link #quoteValue(Object) quoteValue()} to escape and quote
   * each element separately. Otherwise this method is equivalent to
   * {@code set(varName, quoteValue(value))}.
   *
   * @param varName the name of the template variable
   * @param value the value to set the variable to
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#simple(String) SQL.simple()} method
   * @see #quoteValue(Object)
   */
  default SQLSession setValue(String varName, Object value)
        throws UnsupportedOperationException {
    throw notSupported("setValue");
  }

  /**
   * <p>Convenience method, equivalent to
   * {@link #setValue(String, Object) setValue(varName, values)}. Can be used to elegantly
   * populate a SQL IN clause with an arbitrary number of values:
   *
   * <blockquote><pre>{@code
   * SQL sql = SQL.template("SELECT * FROM AIRPORT WHERE NAME IN(~%names%)";
   * try(Connection con = ...) {
   *   List<Airport> airports =  sql.session(con)
   *       .setArray("names", "London Heathrow", "Chicago O'Hare")
   *       .prepareQuery()
   *       .getExtractor()
   *       .extractAll();
   * }
   * }</pre></blockquote>
   *
   * <p>This will execute the following SQL:
   *
   * <blockquote><pre>{@code
   * SELECT * FROM AIRPORT WHERE NAME IN('London Heathrow','Chicago O''Hare')
   * }</pre></blockquote>
   *
   * @param varName the name of the template variable
   * @param values an array, which will be "imploded" to a {@code String}, using a
   *       comma to separate the array elements, and using
   *       {@link #quoteValue(Object) quoteValue()} to escape and quote the array
   *       elements
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#simple(String) SQL.simple()} method
   * @see #quoteValue(Object)
   */
  default SQLSession setArray(String varName, Object... values)
        throws UnsupportedOperationException {
    throw notSupported("setArray");
  }

  /**
   * Convenience method, equivalent to
   * {@link #setValue(String, Object) setValue(varName, values)}.
   *
   * @param varName the name of the template variable
   * @param values an array, which will be "imploded" to a {@code String}, using a
   *       comma to separate the array elements
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#simple(String) SQL.simple()} method
   */
  default SQLSession setArray(String varName, int... values)
        throws UnsupportedOperationException {
    throw notSupported("setArray");
  }

  /**
   * Sets the specified template variable to the escaped and quoted version of the
   * specified identifier (e&#46;g&#46; a column name or table name).
   *
   * @param varName the name of the template variable
   * @param identifier the identifier to substitute the variable with
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#simple(String) SQL.simple()} method
   * @see #quoteIdentifier(String)
   */
  default SQLSession setIdentifier(String varName, String identifier)
        throws UnsupportedOperationException {
    throw notSupported("setIdentifier");
  }

  /**
   * <p>Sets a variable in a <a
   * href="https://github.com/klojang4j/klojang-templates?tab=readme-ov-file#nested-templates">nested
   * template</a> within the SQL skeleton to the specified value. If the value is an array
   * or collection, it will be "imploded" to a string, using {@code "," } (comma) to
   * separate the elements in the array or collection. This method is only supported for
   * {@link SQL#skeleton(String) SQL skeletons}. When calling this method on a
   * {@code SQLSession} obtained via {@link SQL#simple(String) SQL.simple()} or
   * {@link SQL#template(String) SQL.template()}, this method will throw an
   * {@code UnsupportedOperationException}.
   *
   * <p><b>NB Nested templates in general are <i>only</i> supported in SQL skeletons. The
   * are <i>not</i> supported in regular SQL templates.</b>
   *
   * @param path the path to the variable within the SQL skeleton
   * @param value the value to set the variable to
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       <i>not</i> obtained via {@link SQL#skeleton(String) SQL.skeleton()}
   */
  default SQLSession setNested(String path, Object value)
        throws UnsupportedOperationException {
    throw sqlSkeletonsOnly("setNested");
  }

  /**
   * <p>Sets a variable in a <a
   * href="https://github.com/klojang4j/klojang-templates?tab=readme-ov-file#nested-templates">nested
   * template</a> within the SQL skeleton to the escaped and quoted version of the
   * specified value. If the value is an array or collection, it will be "imploded" to a
   * string, using {@code "," } (comma) to separate the elements in the array or
   * collection, and using {@link #quoteValue(Object) quoteValue()} to escape and quote
   * each element separately. This method is only supported for
   * {@link SQL#skeleton(String) SQL skeletons}. When calling this method on a
   * {@code SQLSession} obtained via {@link SQL#simple(String) SQL.simple()} or
   * {@link SQL#template(String) SQL.template()}, this method will throw an
   * {@code UnsupportedOperationException}.
   *
   * <p><b>NB Nested templates in general are <i>only</i> supported in SQL skeletons. The
   * are <i>not</i> supported in regular SQL templates.</b>
   *
   * <blockquote><pre>{@code
   * String sql = """
   *     SELECT * FROM EMPLOYEE
   *     ~%%begin:whereClause%
   *      WHERE FIRST_NAME LIKE ~%searchPhrase% OR LAST_NAME LIKE ~%searchPhrase%
   *     ~%%end:whereClause%
   *     """;
   * try(Connection con = ...) {
   *   SQLSession session = SQL.skeleton(sql).session(con);
   *   if(searchPhrase != null) {
   *     session.setNestedValue("whereClause.searchPhrase", '%' + searchPhrase + '%');
   *   }
   *   try(SQLQuery query = session.prepareQuery()) {
   *     return query.getExtractor(Employee.class).extractAll();
   *   }
   * }
   * }</pre></blockquote>
   *
   * <p>Note that, by default, nested templates are not rendered. They remain invisible
   * unless you do something that makes them become visible. In this case, setting a
   * variable within the nested template forces its entire contents to become visible.
   * Alternatively, you can call {@link #enable(String)} to force the contents of a nested
   * template to become visible.
   *
   * @param path the path to the variable within the SQL skeleton
   * @param value the value to set the variable to
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       <i>not</i> obtained via {@link SQL#skeleton(String) SQL.skeleton()}
   */
  default SQLSession setNestedValue(String path, Object value)
        throws UnsupportedOperationException {
    throw sqlSkeletonsOnly("setNestedValue");
  }

  /**
   * <p>Sets a variable in a <a
   * href="https://github.com/klojang4j/klojang-templates?tab=readme-ov-file#nested-templates">nested
   * template</a> within the SQL skeleton to the escaped and quoted version of the
   * specified identifier (e&#46;g&#46; a column name or table name). This method is only
   * supported for {@link SQL#skeleton(String) SQL skeletons}. When calling this method on
   * a {@code SQLSession} obtained via {@link SQL#simple(String) SQL.simple()} or
   * {@link SQL#template(String) SQL.template()}, this method will throw an
   * {@code UnsupportedOperationException}.
   *
   * <p><b>NB Nested templates in general are <i>only</i> supported in SQL skeletons. The
   * are <i>not</i> supported in regular SQL templates.</b>
   *
   * @param path the path to the variable within the SQL skeleton
   * @param identifier the identifier to substitute the variable with
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       <i>not</i> obtained via {@link SQL#skeleton(String) SQL.skeleton()}
   */
  default SQLSession setNestedIdentifier(String path, String identifier)
        throws UnsupportedOperationException {
    throw sqlSkeletonsOnly("setNestedIdentifier");
  }

  /**
   * Enables the contents of the specified nested template. That is, the contents of the
   * specified nested template will be rendered. By default, nested templates remain
   * invisible unless you do something that makes them become visible. This method is only
   * supported for {@link SQL#skeleton(String) SQL skeletons}. When calling this method on
   * a {@code SQLSession} obtained via {@link SQL#simple(String) SQL.simple()} or
   * {@link SQL#template(String) SQL.template()}, this method will throw an
   * {@code UnsupportedOperationException}.
   *
   * <p><b>NB Nested templates in general are <i>only</i> supported in SQL skeletons. The
   * are <i>not</i> supported in regular SQL templates.</b>
   *
   * <blockquote><pre>{@code
   * String sql = """
   *     SELECT * FROM EMPLOYEE
   *     ~%%begin:whereClause%
   *      WHERE FIRST_NAME LIKE :searchPhrase OR LAST_NAME LIKE :searchPhrase
   *     ~%%end:whereClause%
   *     """;
   * try(Connection con = ...) {
   *   SQLSession session = SQL.skeleton(sql).session(con);
   *   if(searchPhrase != null) {
   *     session.enable("whereClause");
   *   }
   *   try(SQLQuery query = session.prepareQuery()) {
   *     if(searchPhrase != null) {
   *       query.bind("searchPhrase", searchPhrase);
   *     }
   *     return query.getExtractor(Employee.class).extractAll();
   *   }
   * }
   * }</pre></blockquote>
   *
   * @param nestedTemplate the nested template within the SQL skeleton to enable
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       <i>not</i> obtained via {@link SQL#skeleton(String) SQL.skeleton()}
   * @see org.klojang.templates.RenderSession#enable(String...)
   */
  default SQLSession enable(String nestedTemplate) throws UnsupportedOperationException {
    throw sqlSkeletonsOnly("setNested");
  }

  /**
   * Sets the sort column of the ORDER BY clause within a SQL template. This presumes and
   * requires that the template contains a template variable named "orderBy". This is a
   * convenience method facilitating a common use case for template variables: to
   * parametrize the sort column within the ORDER BY clause. It is equivalent to calling
   * {@code setIdentifier("orderBy", sortColumn)}.
   *
   * <blockquote><pre>{@code
   * try(Connection con = ...) {
   *   List<Person> lastNames = SQL.template("SELECT * FROM PERSON ORDER BY ~%orderBy%")
   *       .session(con)
   *       .setOrderBy("LAST_NAME")
   *       .prepareQuery(con)
   *       .getExtractor(Person.class)
   *       .extractAll();
   * }
   * }</pre></blockquote>
   *
   * @param sortColumn the column to sort on
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#simple(String) SQL.simple()} method
   */
  default SQLSession setOrderBy(String sortColumn) throws UnsupportedOperationException {
    throw notSupported("setOrderBy");
  }

  /**
   * Sets the sort column and sort order of the ORDER BY clause within a SQL template.
   * This presumes and requires that the template contains a template variable named
   * "orderBy". This is a convenience method facilitating a common use case for template
   * variables: to parametrize the sort column and sort order within the ORDER BY clause.
   *
   * <blockquote><pre>{@code
   * SQL sql = SQL.template("SELECT LAST_NAME FROM EMPLOYEE ORDER BY ~%orderBy%");
   * try(Connection con = ...) {
   *   // Sort in descending order of salary
   *   List<String> lastNames = sql.session(con).setOrderBy("SALARY", true).prepareQuery().firstColumn();
   * }
   * }</pre></blockquote>
   *
   * @param sortColumn the column to sort on
   * @param isDescending whether to sort in descending order. If true, the
   *       "sortOrder" variable in the SQL template will be set to "DESC", else to "ASC".
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#simple(String) SQL.simple()} method
   */
  default SQLSession setOrderBy(String sortColumn, boolean isDescending)
        throws UnsupportedOperationException {
    throw notSupported("setOrderBy");
  }

  /**
   * <p>Returns a properly escaped and quoted string. More precisely:
   *
   * <ul>
   *     <li>If the value is {@code null}, the literal string {@code "NULL"} (without the
   *         quotes) is returned.
   *     <li>If the value is a {@link Number}, a {@link Boolean}, or a
   *         {@link SQLExpression}, the value is returned as-is. That is,
   *         {@code toString()} will be called on the value, but the resulting string
   *         will <i>not</i> be quoted.
   *     <li>Otherwise {@code toString()} is called on the value, and the resulting string
   *         is escaped and quoted according to the quoting rules of the target database.
   * </ul>
   *
   * <p>Use this method if you do not know or trust the origin of the value to prevent
   * SQL injection.
   *
   * @param value the value to be escaped and quoted
   * @return the escaped and quoted value
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#simple(String) SQL.simple()} method
   * @see java.sql.Statement#enquoteLiteral(String)
   * @see Quoter#quoteValue(Object)
   */
  default String quoteValue(Object value) throws UnsupportedOperationException {
    throw notSupported("quoteValue");
  }

  /**
   * <p>Generates a SQL function call in which each of the function arguments is escaped
   * and quoted using the {@link #quoteValue(Object) quoteValue()} method. If you do not
   * want this to happen for a particular argument, because it is an identifier (a column
   * name) rather than a literal value, wrap it in a {@code SQLExpression} again:
   *
   * <blockquote><pre>{@code
   * SQLExpression column = SQL.expression(session.quoteIdentifier("LAST_NAME"));
   * sqlFunction("CONCAT", "Dear Mr./Ms. ", column, ","));
   * }</pre></blockquote>
   *
   * <p><i>NB This example is just to illustrate the point, but is of course rather silly
   * in practice. You can <b>see</b> here what is being concatenated, and you can
   * <b>see</b> that nothing needs escaping, and that there is no risk of SQL injection,
   * so you might as well simply have written:</i>
   * {@code "CONCAT('Dear Mr./Ms. ', LAST_NAME, ',')"}
   *
   * @param name the name of the function, like {@code "SUBSTRING"} or
   *       {@code "CONCAT"}. Note that the function name is itself not escaped or quoted.
   *       Therefore, with SQL injection in mind, be wary of this being a dynamically
   *       generated value.
   * @param args the function arguments. Each of the provided arguments will pass
   *       through {@link #quoteValue(Object)}.
   * @return a {@code SQLExpression} representing a SQL function call
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#simple(String) SQL.simple()} method
   */
  default SQLExpression sqlFunction(String name, Object... args)
        throws UnsupportedOperationException {
    throw notSupported("sqlFunction");
  }

  /**
   * If necessary, quotes the specified identifier (e&#46;g&#46; a column name or table
   * name) according to the quoting rules of the target database. Use this method if the
   * identifier is passed in from outside your program to prevent SQL injection.
   *
   * @param identifier the identifier to quote
   * @return the quoted identifier
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#simple(String) SQL.simple()} method
   * @see java.sql.Statement#enquoteIdentifier(String, boolean)
   * @see Quoter#quoteIdentifier(String)
   */
  default String quoteIdentifier(String identifier) throws UnsupportedOperationException {
    throw notSupported("quoteIdentifier");
  }

  /**
   * Executes the SQL. You can call this method either if the SQL is completely static, or
   * if you have set all template variables and the SQL does not contain any named
   * parameters. If the SQL <i>does</i> contain named parameters, you must first bind them
   * using a {@link SQLStatement} obtained from the {@code SQLSession}.
   *
   * @return the number of inserted, updated, or deleted rows, if applicable; -1 otherwise
   */
  int execute();

  /**
   * Returns a {@code SQLQuery} instance that allows you to provide values for named
   * parameters and then execute the query.
   *
   * @return a {@code SQLQuery} instance
   */
  SQLQuery prepareQuery();

  /**
   * Returns a {@code SQLInsert} instance that allows you to provide values for named
   * parameters and then execute the INSERT statement. Auto-generated keys will be made
   * available to the client.
   *
   * @return a {@code SQLInsert} instance
   */
  default SQLInsert prepareInsert() {
    return prepareInsert(true);
  }

  /**
   * Returns a {@code SQLInsert} instance that allows you to provide values for named
   * parameters and then execute the INSERT statement.
   *
   * @param retrieveKeys whether to retrieve the keys that were generated by the
   *       database
   * @return a {@code SQLInsert} instance
   */
  SQLInsert prepareInsert(boolean retrieveKeys);

  /**
   * Returns a {@code SQLUpdate} instance that allows you to provide values for named
   * parameters and then execute the UPDATE or DELETE statement.
   *
   * @return a {@code SQLUpdate} instance
   */
  SQLUpdate prepareUpdate();

}
