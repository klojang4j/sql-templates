package org.klojang.jdbc;

import org.klojang.invoke.BeanReader;

import java.util.Collection;
import java.util.List;

/**
 * <p>An {@code SQLSession} allows you to provide values for the <i>template
 * variables</i> within an SQL template. Once you have set all template variables, you can
 * obtain a {@link SQLStatement} object from the {@code SQLSession} and use it to set
 * (a.k.a. "bind") the <i>named parameters</i> within the SQL. The difference between
 * template variables and named parameters is explained in the comments for the
 * {@link SQL SQL interface}. Since the {@code SQLSession} implementation obtained via
 * {@link SQL#simple(String) SQL.simple()}) does not allow for template variables, you
 * have no choice but to retrieve a {@code SQLStatement} from it straight away. If the SQL
 * does not contain any named parameters, you may also call {@link #execute()} on the
 * {@code SQLSession} straight away, without going through the creation of an
 * {@code SQLStatement}.
 *
 * <h2>AutoCloseable</h2>
 * The {@code SQLSession} interface extends {@link AutoCloseable}, so in principle you
 * should set up a try-with-resources block for {@code SQLSession} instances. However, the
 * {@code SQLSession} implementation obtained via {@link SQL#simple(String) SQL.simple()}
 * does not manage any resources that need to be freed up, so a try-with-resources block
 * is optional in that case. Sessions obtained via
 * {@link SQL#template(String) SQL.template()} and
 * {@link SQL#skeleton(String) SQL.skeleton()} will tacitly close the moment you obtain a
 * {@link SQLStatement} from it &#8212; for example via {@link #prepareQuery()}. So,
 * unless an exception occurs, the following code pattern will not cause a resource leak:
 * {@code SQL.template("SELECT ...").session(con).prepareQuery()}. Even though closed
 * after obtaining a {@code SQLStatement} from it, you can still re-use the session
 * because the required resources will be created again if and when necessary. With SQL
 * skeletons there is no gain to be had from re-using sessions. With SQL templates,
 * however, you do save the (small) cost of extracting named parameters from the SQL
 * string.
 */
public sealed interface SQLSession extends AutoCloseable permits AbstractSQLSession {

  /**
   * Sets the specified template variable to the specified value. Only use this method if
   * you know and trust the source of the provided value. The value will not be escaped or
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
   * specified value. Use this method if you do not know or trust the source of the value
   * to prevent SQL injection. If the value is an array or collection, it will be
   * "imploded" to a string, using {@code "," } (comma) to separate the elements in the
   * array or collection, and using {@link #quoteValue(Object) quoteValue()} to escape and
   * quote each element separately. Otherwise this method is equivalent to
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
   * {@link SQL#skeleton(String) SQL.skeleton()}. The SQL template must contain a nested
   * template named "record". This template will be repeated for each of the beans or
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
   *   try(SQLSession session = sql.session(con)) {
   *     session.setValues(persons, processor).execute();
   *   }
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
   * <p>And then simply call {@code session.setValues(persons)} (obviating the need to
   * create and use a {@code BeanValueProcessor}).
   *
   * @param <T> the type of the beans or records to persist
   * @param beans the beans or records to persist (at least one required)
   * @param processor a {@code BeanValueProcessor} that allows you to selectively
   *       convert values within the provided beans or records
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#simple(String) SQL.simple()} or
   *       {@link SQL#template(String) SQL.template()} method
   * @see SQLInsert#insertBatchAndGetIDs(List)
   * @see SQLInsert#insertBatchAndSetIDs(String, List)
   */
  default <T> SQLSession setValues(List<T> beans, BeanValueProcessor<T> processor)
        throws UnsupportedOperationException {
    throw new UnsupportedOperationException(sqlSkeletonsOnly());
  }

  /**
   * Sets the sort column of the ORDER BY clause within a SQL template. This presumes and
   * requires that the template contains a template variable named "orderBy". This is a
   * convenience method facilitating a common use case for template variables:
   * parametrizing the sort column within the ORDER BY clause. It is equivalent to calling
   * {@code setIdentifier("orderBy", sortColumn)}.
   *
   * @param sortColumn the column to sort on
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#simple(String) SQL.simple()} method
   */
  default SQLSession setOrderBy(String sortColumn) throws UnsupportedOperationException {
    return setIdentifier("orderBy", sortColumn);
  }

  /**
   * Sets the sort column and sort order of the ORDER BY clause within a SQL template.
   * This presumes and requires that the template contains a template variable named
   * "orderBy". This is a convenience method facilitating a common use case for template
   * variables: parametrizing the sort column and sort order within the ORDER BY clause.
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
    String orderBy = quoteIdentifier(sortColumn) + (isDescending ? " DESC" : " ASC");
    return set("orderBy", orderBy);
  }

  /**
   * <p>Escapes and quotes the specified value. More precisely:
   * <ul>
   *     <li>If the value is {@code null}, the literal string "NULL"
   *         (<i>without</i> quotes) is returned.
   *     <li>If the value is a {@link Number}, a {@link Boolean}, or a
   *         {@link SQLExpression}, the value is returned as-is. That is,
   *         {@code toString()} will be called on the value, but the resulting string
   *         will <i>not</i> be quoted.
   *     <li>Otherwise the value is escaped and quoted according to the quoting rules of
   *         the target database.
   * </ul>
   * <p>Use this method if you do not know or trust the source of the value to prevent
   * SQL injection.
   *
   * @param value the value to be escaped and quoted
   * @return the escaped and quoted value
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#simple(String) SQL.simple()} method
   * @see java.sql.Statement#enquoteLiteral(String)
   */
  default String quoteValue(Object value) throws UnsupportedOperationException {
    throw notSupported("quoteValue");
  }

  /**
   * Generates a SQL function call in which each of the function arguments is escaped and
   * quoted using the {@link #quoteValue(Object) quoteValue()} method.
   *
   * @param name the name of the function
   * @param args the function arguments. Each of the provided arguments will be
   *       escaped and quoted using {@link #quoteValue(Object)}.
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
   */
  default String quoteIdentifier(String identifier) throws UnsupportedOperationException {
    throw notSupported("quoteIdentifier");
  }

  /**
   * Executes the SQL. You can call this method either if the SQL is completely static, or
   * if you have set all template variables and the SQL does not contain any named
   * parameters. If the SQL <i>does</i> contain named parameters, you will first have to
   * bind them. Call {@link #prepareInsert()}, or {@link #prepareUpdate()} to obtain a
   * {@link SQLStatement} that enables you to bind the named parameters. Although you
   * <i>could</i> call this method for SQL SELECT statements, it does not make much sense
   * because this method does not return any feedback from the database.
   */
  void execute();

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
