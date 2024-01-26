package org.klojang.jdbc;

import org.klojang.invoke.BeanReader;

import java.util.Collection;
import java.util.List;

/**
 * <p>An {@code SQLSession} allows you to provide values for the <a
 * href="https://klojang4j.github.io/klojang-templates/api/org.klojang.templates/module-summary.html">template
 * variables</a> within the SQL. Once you have set all template variables, you can obtain
 * a {@link SQLStatement} object from the {@code SQLSession} and use it to set (a.k.a.
 * "bind") the <i>named parameters</i> within the SQL. The difference between template
 * variables and named parameters is explained in the comments for the {@link SQL}
 * interface. Since the {@code SQLSession} implementation obtained via
 * {@link SQL#simple(String) SQL.simple()}) does not allow for template variables, you
 * have no choice but to retrieve a {@code SQLStatement} from it straight away. If the SQL
 * does not contain any named parameters, you may call {@link #execute()} on the
 * {@code SQLSession} straight away, without going through the creation of an
 * {@code SQLStatement}.
 *
 * <h2>AutoCloseable</h2>
 * The {@code SQLSession} interface extends {@link AutoCloseable}, so in principle you
 * should set up a try-with-resources block for {@code SQLSession} instances. However, the
 * {@code SQLSession} implementation obtained via {@link SQL#simple(String) SQL.simple()}
 * does manage any resources that need to be freed up, so in that case a
 * try-with-resources block is optional.
 */
public sealed interface SQLSession extends AutoCloseable permits AbstractSQLSession {

  /**
   * Sets the value of the specified template variable. The value will not be escaped or
   * quoted. If the value is an array or collection, it will be "imploded" to a string,
   * using {@code "," } (comma) to separate the elements in the array or collection. This
   * method will throw an {@link UnsupportedOperationException} for
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
   * specified value. The value is escaped and quoted using the
   * {@link #quoteValue(Object)} method. Use this method if the source of the value is
   * unknown to prevent SQL injection.
   *
   * @param varName the name of the template variable
   * @param value the value to set the variable to
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#simple(String) SQL.simple()} method
   * @see #quoteValue(Object)
   */
  default SQLSession quote(String varName, Object value)
        throws UnsupportedOperationException {
    throw notSupported("quote");
  }

  /**
   * <p>A specialised templating method, aimed at facilitating batch inserts. This method
   * is <i>only</i> supported by {@code SQL} instances obtained via
   * {@link SQL#skeleton(String) SQL.skeleton()}. The SQL template must contain a nested
   * template named "record". This template will be repeated for each of the beans or
   * records in the provided array. See {@link #setValues(BeanReader, List)} for a usage
   * example.
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
   * <p>A specialised templating method, aimed at facilitating batch inserts. This method
   * is <i>only</i> supported by {@code SQL} instances obtained via
   * {@link SQL#skeleton(String) SQL.skeleton()}. The SQL template must contain a nested
   * template named "record". This template will be repeated for each of the beans or
   * records in the provided array. See {@link #setValues(BeanReader, List)} for a usage
   * example. This method creates a {@link BeanReader} for the class of the <i>first</i>
   * bean or record in the provided list and then, in essence, calls
   * {@code setValues(reader, beans)}.
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
   * <p>A specialised templating method, aimed at facilitating batch inserts. This method
   * is <i>only</i> supported by {@code SQL} instances obtained via
   * {@link SQL#skeleton(String) SQL.skeleton()}. The SQL template must contain a nested
   * template named "record". This template will be repeated for each of the beans or
   * records in the provided list.
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
   * // ...
   *
   * SQL sql = SQL.skeleton("""
   *    INSERT INTO PERSON(FIRST_NAME,LAST_NAME,AGE) VALUES
   *    ~%%begin:record%
   *    (~%firstName%,~%lastName%,~%age%)
   *    ~%%end:record%
   *    """);
   *
   * BeanReader<Person> reader = new BeanReader<>(Person.class);
   * try(Connection con = ...) {
   *   try(SQLSession session = sql.session(con)) {
   *     session.setValues(reader, persons).execute();
   *   }
   * }
   * }</pre></blockquote>
   *
   * <p>The above code snippet will execute the following SQL:
   * <blockquote><pre>{@code
   * INSERT INTO PERSON(FIRST_NAME,LAST_NAME,AGE) VALUES
   * ('John','Smith',34),
   * ('Francis','O''Donell',27),
   * ('Mary','Bear',52)
   * }</pre></blockquote>
   *
   * @param <T> the type of the beans or records to persist
   * @param reader a {@code BeanReader} for the beans or records to persist
   * @param beans the beans or records to persist (at least one required)
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#simple(String) SQL.simple()} or
   *       {@link SQL#template(String) SQL.template()} method
   * @see SQLInsert#insertBatchAndGetIDs(List)
   * @see SQLInsert#insertBatchAndSetIDs(String, List)
   */
  default <T> SQLSession setValues(BeanReader<T> reader, List<T> beans)
        throws UnsupportedOperationException {
    throw new UnsupportedOperationException(sqlSkeletonsOnly());
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
   *       obtained via the {@link SQL#simple(String) SQL.simple()} method
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
   *       obtained via the {@link SQL#simple(String) SQL.simple()} method
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
   *       obtained via the {@link SQL#simple(String) SQL.simple()} method
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
   *       obtained via the {@link SQL#simple(String) SQL.simple()} method
   */
  default SQLSession setOrderBy(Object sortColumn, Object sortOrder)
        throws UnsupportedOperationException {
    return setOrderBy(sortColumn).setSortOrder(sortOrder);
  }

  /**
   * Sets the sort column and sort order of an ORDER BY clause.
   *
   * @param sortColumn the column to sort on
   * @param isDescending whether to sort in descending order
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#simple(String) SQL.simple()} method
   */
  default SQLSession setOrderBy(Object sortColumn, boolean isDescending)
        throws UnsupportedOperationException {
    return setOrderBy(sortColumn).setDescending(isDescending);
  }

  /**
   * <p>Escapes and quotes the specified value. More precisely:
   * <ul>
   *     <li>If the value is {@code null}, the literal string "NULL"
   *         (<i>without</i> quotes) is returned.
   *     <li>If the value is a {@link Number}, a {@link Boolean} or a
   *         {@link SQLExpression}, the value is returned as-is. That is,
   *         {@code toString()} will be called on the value, but the resulting string
   *         will <i>not</i> be quoted.
   *     <li>Otherwise the value is escaped and quoted according to the quoting rules of
   *         the target database.
   * </ul>
   * <p>Use this method if the value is passed in from outside your program to prevent
   * SQL injection.
   *
   * @param value the value to be escaped and quoted
   * @return the escaped and quoted value
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#simple(String) SQL.simple()} method
   * @see java.sql.Statement#enquoteLiteral(String)
   */
  default String quoteValue(Object value) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /**
   * Enables you to embed SQL function calls (a subset of SQL expressions) in your SQL
   * without risking SQL injection.
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
   * // ...
   *
   * SQL sql = SQL.skeleton("""
   *    INSERT INTO PERSON(FIRST_NAME,LAST_NAME,AGE) VALUES
   *    ~%%begin:record%
   *    (~%firstName%,~%lastName%,~%age%)
   *    ~%%end:record%
   *    """);
   *
   * try (Connection con = ...) {
   *   try (SQLSession session = sql.session(con)) {
   *     BeanValueTransformer<Person> transformer = (bean, prop, val) -> {
   *         if (prop.equals("firstName")) {
   *           return session.sqlFunction("SUBSTRING", val, 1, 3);
   *         }
   *         return val;
   *     };
   *     BeanReader<Person> reader = new BeanReader<>(Person.class, transformer);
   *     session.setValues(reader, persons).execute();
   *     String query = "SELECT FIRST_NAME FROM PERSON";
   *     List<String> firstNames = SQL.simpleQuery(con, query).firstColumn();
   *     assertEquals(List.of("Joh", "Fra", "Mar"), firstNames);
   *   }
   * }
   * }</pre></blockquote>
   *
   * <p>The above code snippet will execute the following SQL:
   * <blockquote><pre>{@code
   * INSERT INTO PERSON(FIRST_NAME,LAST_NAME,AGE) VALUES
   * (SUBSTRING('John', 1, 3), 'Smith', 34),
   * (SUBSTRING('Francis', 1, 3), 'O''Donell', 27),
   * (SUBSTRING('Mary', 1, 3), 'Bear', 52)
   * }</pre></blockquote>
   *
   *
   * @param name the name of the function
   * @param args the function arguments. Each of the provided arguments will be
   *       escaped and quoted using {@link #quoteValue(Object)}.
   * @return
   */
  default SQLExpression sqlFunction(String name, Object... args) {
    throw new UnsupportedOperationException();
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

  private static UnsupportedOperationException notSupported(String method) {
    String fmt = "method %s() only supported for SQL templates and SQL skeletons";
    return new UnsupportedOperationException(String.format(fmt, method));
  }

  private static String sqlSkeletonsOnly() {
    return "setValues() only supported for SQL skeletons";
  }

}
