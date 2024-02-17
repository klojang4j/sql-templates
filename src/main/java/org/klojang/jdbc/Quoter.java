package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.JDBC;
import org.klojang.jdbc.x.Utils;
import org.klojang.util.ArrayMethods;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.klojang.check.Tag.VARARGS;
import static org.klojang.jdbc.x.Strings.FUNCTION_NAME;
import static org.klojang.jdbc.x.Strings.IDENTIFIER;
import static org.klojang.util.StringMethods.append;

/**
 * <p>Escapes and quotes values according to the quoting rules of the target database.
 * You do not normally need to concern yourself with quoting and escaping unless you want
 * to dynamically generate and inject a {@link SQLExpression SQL expression} in the SQL to
 * be executed. For example, suppose (somewhat unrealistically) you have a variable
 * {@code firstName} in your program, and you want to embed the following expression in
 * your SQL: <b>{@code "SUBSTRING('" + firstName + "', 1, 3)"}</b>. If the value of
 * {@code firstName} comes from outside your program, you expose yourself to the risk of
 * SQL injection. In this case you should use a {@code Quoter} to eliminate the risk:
 * <b>{@code "SUBSTRING(" + quoter.quoteValue(firstName) + ", 1, 3)"}</b>. For SQL
 * function calls in particular, you can also use
 * {@link #sqlFunction(String, Object...) Quoter.sqlFunction()}:
 * <b>{@code quoter.sqlFunction("SUBSTRING", firstName, 1, 3)}</b>.
 *
 * <p>Only use a {@code Quoter} to escape and quote strings <b>within</b> the SQL
 * expression. In any other case escaping and quoting is taken care of by <i>Klojang
 * JDBC</i>.
 *
 * @see SQLExpression
 * @see SQLSession#quoteValue(Object)
 * @see Statement#enquoteLiteral(String)
 * @see Statement#enquoteIdentifier(String, boolean)
 */
public final class Quoter {

  private final Statement stmt;

  Quoter(Statement stmt) {
    this.stmt = stmt;
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
   * @param value the value to quote
   * @return a properly escaped and quoted string
   * @see Statement#enquoteLiteral(String)
   */
  public String quoteValue(Object value) {
    try {
      return JDBC.quote(stmt, value);
    } catch (SQLException e) {
      throw Utils.wrap(e);
    }
  }

  /**
   * If necessary, quotes the specified identifier (e&#46;g&#46; a column name or table
   * name) according to the quoting rules of the target database. Use this method if the
   * identifier is passed in from outside your program to prevent SQL injection.
   *
   * @param identifier the identifier to quote
   * @return the quoted identifier
   * @see java.sql.Statement#enquoteIdentifier(String, boolean)
   */
  public String quoteIdentifier(String identifier) {
    Check.notNull(identifier, IDENTIFIER);
    try {
      return stmt.enquoteIdentifier(identifier, false);
    } catch (SQLException e) {
      throw Utils.wrap(e);
    }
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
   */
  public SQLExpression sqlFunction(String name, Object... args) {
    Check.notNull(name, FUNCTION_NAME);
    Check.notNull(args, VARARGS);
    String str = ArrayMethods.implode(args, this::quoteValue, ",");
    String expr = append(new StringBuilder(), name, '(', str, ')').toString();
    return new SQLExpression(expr);
  }
}
