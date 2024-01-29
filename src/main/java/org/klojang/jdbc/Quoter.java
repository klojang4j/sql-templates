package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.JDBC;
import org.klojang.jdbc.x.Utils;
import org.klojang.util.ArrayMethods;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.klojang.check.Tag.VARARGS;
import static org.klojang.util.StringMethods.append;

/**
 * <p>Escapes and quotes values according to the quoting rules of the target database.
 * You do not normally need to concern yourself with quoting and escaping unless you want
 * to generate an {@link SQLExpression SQL expression} from one or more values in your
 * program. For example, suppose you have a variable {@code firstName} in your program,
 * and you want to embed the following expression in your SQL:
 * <b>{@code "SUBSTRING(" + firstName + ", 1, 3)"}</b>. If the value of {@code firstName}
 * comes from outside your program, you expose yourself to the risk of SQL injection. In
 * this case you should use a {@code Quoter} to eliminate the risk:
 * <b>{@code "SUBSTRING(" + quoter.quoteValue(firstName) + ", 1, 3)"}</b>.
 *
 * <p><b>Only use a {@code Quoter} to escape and quote strings <i>within</i> the SQL
 * expression</b>. In any other case escaping and quoting is taken care of by <i>Klojang
 * JDBC</i>, or simply by JDBC itself.
 *
 * @see SQLExpression
 * @see SQLSession#setValues(List, BeanValueProcessor)
 * @see SQLSession#quoteValue(Object)
 * @see Statement#enquoteLiteral(String)
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
   *     <li>If the value is {@code null}, the literal string "NULL"
   *         (<i>without</i> quotes) is returned.
   *     <li>If the value is a {@link Number}, a {@link Boolean}, or a
   *         {@link SQLExpression}, the value is returned as-is. That is,
   *         {@code toString()} will be called on the value, but the resulting string
   *         will <i>not</i> be quoted.
   *     <li>Otherwise the value is escaped and quoted according to the quoting rules of
   *         the target database.
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
   * Generates a SQL function call in which each of the function arguments is escaped and
   * quoted using the {@link #quoteValue(Object) quoteValue()} method.
   *
   * @param name the name of the function
   * @param args the function arguments. Each of the provided arguments will be
   *       escaped and quoted using {@link #quoteValue(Object)}.
   * @return an {@code SQLExpression} representing a SQL function call
   */
  public SQLExpression sqlFunction(String name, Object... args) {
    Check.notNull(name, "SQL function name");
    Check.notNull(args, VARARGS);
    String str = ArrayMethods.implode(args, this::quoteValue, ",");
    String expr = append(new StringBuilder(), name, '(', str, ')').toString();
    return new SQLExpression(expr);
  }
}
