package org.klojang.jdbc;

import org.klojang.invoke.BeanReader;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * An {@code SQLSession} is used to initiate and prepare the execution of SQL. It allows
 * you to set SQL <i>template variables</i> within the SQL and then obtain a
 * {@link SQLStatement} object that can be used to set (a.k.a. "bind") the <i>named
 * parameters</i> within the SQL. The difference between template variables and named
 * parameters is explained in the comments for the {@link SQL} interface. The
 * {@code SQLSession} implementation you get from {@link SQL#basic(String) SQL.basic()})
 * does not support template variables. This leaves you no choice but to retrieve a
 * {@code SQLStatement} from it straight away.
 *
 * <p>
 * Probably the most common use case for using template variables is to parametrize the
 * ORDER BY column and sort order ("ASC" or "DESC"). Therefore the {@code SQLSession}
 * contains a few specialized {@code set} methods specifically for this purpose. These
 * methods assume that the SQL template contains a template variable named "sortColumn"
 * for the ORDER BY column(s), and a template variable name "sortOrder" for the sort
 * order.
 *
 * <p>
 * The difference between the {@code SQLSession} you get from
 * {@link SQL#template(String) SQL.template()} and the one you get from
 * {@link SQL#skeleton(String) SQL.skeleton()} is that with the latter, named parameters
 * are extracted from the SQL at the very last moment, just before you retrieve a
 * {@link SQLStatement} from the session. Thus, if the SQL contained template variables,
 * and you set one or more of them to text values that again contain named parameters,
 * these, too, will be available for binding in the {@code SQLStatement}.
 *
 * <p>
 * <i>An {@code SQLSession} is not thread-safe and should generally not be
 * reused once you have obtained a {@code SQLStatement} object from it.</i>
 */

/*
 * In fact all implementations currently _are_ thread-safe, but they are not intended to
 * be, and we don't want to commit to it.
 */
public sealed interface SQLSession extends AutoCloseable permits AbstractSQLSession {

  /**
   * Sets the value of the specified template variable. The value will not be escaped or
   * quoted. If the value is an array or collection, it will be "imploded" to a string,
   * using {@code "," } (comma) to separate the elements in the array or collection. This
   * method will throw an {@link UnsupportedOperationException} for
   * {@linkplain SQL#basic(String) basic SQL sessions} since these are not based on <a
   * href="https://klojang4j.github.io/klojang-templates/1/api/org.klojang.templates/module-summary.html">Klojang
   * Templates</a>.
   *
   * @param varName the name of the template variable
   * @param value the value to set the variable to.
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
   * Sets the specified template variable to the escaped and quoted version of the
   * specified value. The value is escaped and quoted using the
   * {@link #quoteValue(Object)} method. Use this method if the source of the value is
   * unknown to prevent SQL injection.
   *
   * @param varName the name of the template variable
   * @param value the value to set the variable to
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#basic(String) SQL.basic()} method
   * @see #quoteValue(Object)
   */
  default SQLSession quote(String varName, Object value)
        throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /**
   * <p>A specialised SQL template method, aimed at facilitating batch inserts. The SQL
   * template must contain a nested template named "values". This template will be
   * repeated for each of the beans or records in the provided list. This is best
   * illustrated using an example:
   *
   * <blockquote><pre>{@code
   * record Person(Integer id, String firstName, String lastName, int age) {}
   * // ...
   * List<Person> persons = List.of(
   *    new Person(null, "John", "Smith", 34),
   *    new Person(null, "Francis", "O'Donell", 27),
   *    new Person(null, "Mary", "Bear", 52));
   * // ...
   * SQL sql = SQL.template("""
   *    INSERT INTO PERSON(FIRST_NAME,LAST_NAME,AGE) VALUES
   *    ~%%begin:values%
   *    (~%firstName%,~%lastName%,~%age%)
   *    ~%%end:values%
   *    """);
   * try(SQLSession session = sql.session()) {
   *  session.setValues(Person.class, persons).execute();
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
   * @param beans the beans or records to persist
   * @return this {@code SQLSession} instance
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#basic(String) SQL.basic()} method
   * @see SQLInsert#insertBatchAndGetIDs(List)
   * @see SQLInsert#insertBatchAndSetIDs(String, List)
   */
  default <T> SQLSession setValues(BeanReader<T> reader, List<T> beans)
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
   * Sets the sort column and sort order of an ORDER BY clause.
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
   * Escapes and quotes the specified value. More precisely:
   * <ul>
   * <li>If the value is {@code null}, the literal string "NULL"
   * (<i>without</i> quotes) is returned.
   * <li>If the value is a {@link Number}, a {@link Boolean} or a
   * {@link SQLExpression}, the value is returned as-is. No quoting will take
   * place.
   * <li>Otherwise the value is escaped and quoted according to the quoting
   * rules of the target database.
   * </ul>
   * Use this method if the value comes from outside your program to prevent
   * SQL injection.
   *
   * @param value the value to be escaped and quoted
   * @return the escaped and quoted value
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#basic(String) SQL.basic()} method
   */
  default String quoteValue(Object value) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /**
   * If necessary, quotes the specified identifier (e&#46;g&#46; a column name or table
   * name) according to the quoting rules of the target database.
   *
   * @param identifier the identifier to quote
   * @return the quoted identifier
   * @throws UnsupportedOperationException in case this {@code SQLSession} was
   *       obtained via the {@link SQL#basic(String) SQL.basic()} method
   * @see java.sql.Statement#enquoteIdentifier(String, boolean)
   */
  default String quoteIdentifier(String identifier) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
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

}
