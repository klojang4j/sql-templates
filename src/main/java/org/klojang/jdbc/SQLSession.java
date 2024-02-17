package org.klojang.jdbc;

import org.klojang.invoke.BeanReader;

import java.util.Collection;
import java.util.List;

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
   * @param value the value to set the variable to.
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
   * <p>Equivalent to {@link #setValue(String, Object) setValue(varName, values)}. Can be
   * used to elegantly populate an SQL IN clause with an arbitrary number of values:
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
   * Equivalent to {@link #setValue(String, Object) setValue(varName, values)}.
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
   * <p>Sets the contents of the VALUES clause within an INSERT statement. This method is
   * only supported by {@code SQL} instances obtained via
   * {@link SQL#skeleton(String) SQL.skeleton()}. Equivalent to
   * {@link #setValues(List, BeanValueProcessor) setValues(Arrays.asList(beans),
   * BeanValueProcessor.identity())}.
   *
   * @param <T> the type of the beans or records to persist
   * @param beans the beans or records to persist (at least one required)
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#simple(String) SQL.simple()} or
   *       {@link SQL#template(String) SQL.template()} method
   */
  @SuppressWarnings("unchecked")
  default <T> SQLSession setValues(T... beans) throws UnsupportedOperationException {
    throw new UnsupportedOperationException(sqlSkeletonsOnly());
  }

  /**
   * <p>Sets the contents of the VALUES clause within an INSERT statement. This method is
   * only supported by {@code SQL} instances obtained via
   * {@link SQL#skeleton(String) SQL.skeleton()}. Equivalent to
   * {@link #setValues(List, BeanValueProcessor) setValues(beans,
   * BeanValueProcessor.identity())}.
   *
   * <blockquote><pre>{@code
   * record Person(Integer id, String firstName, String lastName, int age) {}
   *
   * // ...
   *
   * List<Person> persons = List.of(
   *    new Person(null, "John", "Smith", 34),
   *    new Person(null, "Francis", "O'Donell", 27),
   *    new Person(null, "Mary", "Bear", 52));
   *
   * SQL sql = SQL.skeleton("""
   *    INSERT INTO PERSON(FIRST_NAME,LAST_NAME,AGE) VALUES
   *    ~%%begin:record%
   *    (~%firstName%,~%lastName%,~%age%)
   *    ~%%end:record%
   *    """);
   *
   * try(Connection con = ...) {
   *   sql.session(con).setValues(persons).execute();
   * }
   * }</pre></blockquote>
   *
   *
   * <p>The above code snippet will execute the following SQL:
   * <blockquote><pre>{@code
   * INSERT INTO PERSON(FIRST_NAME,LAST_NAME,AGE) VALUES
   * ('John', 'Smith', 34),
   * ('Francis', 'O''Donell', 27),
   * ('Mary', 'Bear', 52)
   * }</pre></blockquote>
   *
   * @param <T> the type of the beans or records to persist
   * @param beans the beans or records to persist (at least one required).
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#simple(String) SQL.simple()} or
   *       {@link SQL#template(String) SQL.template()} method
   */
  default <T> SQLSession setValues(List<T> beans) throws UnsupportedOperationException {
    throw new UnsupportedOperationException(sqlSkeletonsOnly());
  }

  /**
   * <p>Sets the contents of the VALUES clause within an INSERT statement. This method is
   * only supported by {@code SQL} instances obtained via
   * {@link SQL#skeleton(String) SQL.skeleton()}. The SQL template must contain a
   * <a
   * href="https://github.com/klojang4j/klojang-templates?tab=readme-ov-file#nested-templates">nested
   * template</a> named "record". This template will be repeated for each of the beans or
   * records in the provided list. This is best illustrated using an example:
   *
   * <blockquote><pre>{@code
   * record Person(Integer id, String firstName, String lastName, int age) {}
   *
   * // ...
   *
   * List<Person> persons = List.of(
   *    new Person(null, "John", "Smith", 34),
   *    new Person(null, "Francis", "O'Donell", 27),
   *    new Person(null, "Mary", "Bear", 52));
   *
   * SQL sql = SQL.skeleton("""
   *    INSERT INTO PERSON(FIRST_NAME,LAST_NAME,AGE) VALUES
   *    ~%%begin:record%
   *    (~%firstName%,~%lastName%,~%age%)
   *    ~%%end:record%
   *    """);
   *
   * BeanValueProcessor processor = (bean, prop, val, quoter) -> {
   *     if(prop.equals("firstName") {
   *       return quoter.sqlFunction("SUBSTRING", val, 1, 3);
   *     }
   *     return val;
   * };
   *
   * try(Connection con = ...) {
   *   sql.session(con).setValues(persons, processor).execute();
   *   String query = "SELECT FIRST_NAME FROM PERSON";
   *   List<String> firstNames = SQL.simpleQuery(con, query).firstColumn();
   *   assertEquals(List.of("Joh", "Fra", "Mar"), firstNames);
   * }
   * }</pre></blockquote>
   *
   *
   * <p>The above code snippet will execute the following SQL:
   * <blockquote><pre>{@code
   * INSERT INTO PERSON(FIRST_NAME,LAST_NAME,AGE) VALUES
   * (SUBSTRING('John', 1, 3), 'Smith', 34),
   * (SUBSTRING('Francis', 1, 3), 'O''Donell', 27),
   * (SUBSTRING('Mary', 1, 3), 'Bear', 52)
   * }</pre></blockquote>
   *
   * <p>Beware of mixing multiple types of elements within the {@code List} of beans.
   * Under the hood this method creates a {@link BeanReader} for the type of the first
   * element in the {@code List}. The type of subsequent elements may be a subtype of that
   * type, but not a supertype (unless you only read properties that belong to the
   * supertype).
   *
   * <p>Also note that the above example is just for illustration purposes. It would
   * have been much easier to use the following SQL template:
   *
   * <blockquote><pre>{@code
   * SQL sql = SQL.skeleton("""
   *    INSERT INTO PERSON(FIRST_NAME,LAST_NAME,AGE) VALUES
   *    ~%%begin:record%
   *    (SUBSTRING(~%firstName%, 1, 3),~%lastName%,~%age%)
   *    ~%%end:record%
   *    """);
   * }</pre></blockquote>
   *
   * <p>And then simply call {@code session.setValues(persons)} (thus obviating the need
   * to create and use a {@code BeanValueProcessor}).
   *
   * @param <T> the type of the beans or records to persist
   * @param beans the beans or records to persist (at least one required)
   * @param processor a {@code BeanValueProcessor} that allows you to selectively
   *       convert values within the provided beans or records
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#simple(String) SQL.simple()} or
   *       {@link SQL#template(String) SQL.template()} method
   * @see SQLInsert#insertBatch(List)
   * @see SQLBatchInsert
   */
  default <T> SQLSession setValues(List<T> beans, BeanValueProcessor<T> processor)
        throws UnsupportedOperationException {
    throw new UnsupportedOperationException(sqlSkeletonsOnly());
  }

  /**
   * <p>Sets the contents of the VALUES clause within an INSERT statement and then
   * executes the SQL. This method is only supported by {@code SQL} instances obtained via
   * {@link SQL#skeleton(String) SQL.skeleton()}. The SQL template must contain a nested
   * template named "record". This template will be repeated for each of the beans or
   * records in the provided list. The SQL statement must not contain named parameters,
   * and any template variables outside the nested "record" template must be set first.
   *
   * @param <T> the type of the beans or records to persist
   * @param beans the beans or records to persist (at least one required)
   * @return the keys generated by the database
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#simple(String) SQL.simple()} or
   *       {@link SQL#template(String) SQL.template()} method
   * @see SQLInsert#insertBatch(List)
   * @see SQLBatchInsert
   */
  default <T> long[] setValuesAndExecute(List<T> beans)
        throws UnsupportedOperationException {
    throw new UnsupportedOperationException(sqlSkeletonsOnly());
  }

  /**
   * <p>Sets the contents of the VALUES clause within an INSERT statement and then
   * executes the SQL. This method is only supported by {@code SQL} instances obtained via
   * {@link SQL#skeleton(String) SQL.skeleton()}. The SQL template must contain a nested
   * template named "record". This template will be repeated for each of the beans or
   * records in the provided list. The SQL statement must not contain named parameters,
   * and any template variables outside the nested "record" template must be set first.
   *
   * @param <T> the type of the beans or records to persist
   * @param beans the beans or records to persist (at least one required)
   * @param processor a {@code BeanValueProcessor} that allows you to selectively
   *       convert values within the provided beans or records
   * @return the keys generated by the database
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#simple(String) SQL.simple()} or
   *       {@link SQL#template(String) SQL.template()} method
   * @see SQLInsert#insertBatch(List)
   * @see SQLBatchInsert
   */
  default <T> long[] setValuesAndExecute(List<T> beans, BeanValueProcessor<T> processor)
        throws UnsupportedOperationException {
    throw new UnsupportedOperationException(sqlSkeletonsOnly());
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
   * Generates a SQL function call in which each of the function arguments is escaped and
   * quoted using the {@link #quoteValue(Object) quoteValue()} method.
   *
   * @param name the name of the function, like {@code "SUBSTRING"} or
   *       {@code "CONCAT"}. Note that this argument is not processed or checked in any
   *       way. Therefore, with SQL injection in mind, be wary of this being a dynamically
   *       generated value.
   * @param args the function arguments. Each of the provided arguments will pass
   *       through {@link #quoteValue(Object)}.
   * @return an {@code SQLExpression} representing a SQL function call
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

  private static UnsupportedOperationException notSupported(String method) {
    String fmt = "method %s() only supported for SQL templates and SQL skeletons";
    return new UnsupportedOperationException(String.format(fmt, method));
  }

  private static String sqlSkeletonsOnly() {
    return "setValues() only supported for SQL skeletons";
  }

}
