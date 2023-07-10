package org.klojang.jdbc;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * <p>Escapes and quotes values according to the target database's quoting and escaping
 * rules. Quoters can be employed when executing {@link SQLBatchInsert batch inserts}. You
 * will not ordinarily have to concern yourself with quoting and escaping unless you want
 * to {@link Transformer#transform(Object, Object, Quoter) tranform} a value into a
 * {@link SQLExpression SQL expression} (i.e. a function call) before saving it to the
 * database. SQL expressions will be injected as-is into the INSERT statement.
 * <i>Klojang JDBC</i> will not attempt to "understand" them. That makes you responsible
 * for protecting yourself against SQL injection. The {@code Quoter} class is meant to
 * make it easy for you to do so.
 *
 * <p>Here is a clear (but not very realistic) example of where and how you can use a
 * {@code Quoter}:
 *
 * <blockquote><pre>{@code
 * SQLBatchInsertBuilder builder = SQL.prepareBatchInsert()
 *    .withTransformer("lastName", (bean, value, quoter) -> {
 *      String quoted = quoter.escapeAndQuote(value);
 *      String expression = String.format("LTRIM(%s)", quoted);
 *      return SQL.expression(expression);
 *    });
 * }</pre></blockquote>
 */
public final class Quoter {

  private final Statement stmt;

  Quoter(Statement stmt) {
    this.stmt = stmt;
  }

  /**
   * <p>Returns a properly escaped and quoted string. This method behaves as follows:
   *
   * <ul>
   *   <li>if the argument is {@code null}, this method returns the string literal "NULL"
   *   <li>if the argument is a {@link Number} or a {@link Boolean}, this method returns
   *   {@code value.toString()} (in other words, it returns an <i>unquoted</i> string)
   *   <li>else this method returns the result of quoting and escaping
   *   {@code value.toString()} according to the target database's quoting and escaping
   *   rules
   * </ul>
   *
   * @param value the value to quote
   * @return a properly escaped and quoted string
   * @throws SQLException if a database error occurs
   * @see Statement#enquoteLiteral(String)
   */
  public String escapeAndQuote(Object value) throws SQLException {
    if (value == null) {
      return "NULL";
    } else if (value instanceof Number || value instanceof Boolean) {
      return value.toString();
    }
    return stmt.enquoteLiteral(value.toString());
  }
}
