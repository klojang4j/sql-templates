package org.klojang.jdbc;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * <p>Escapes and quotes values according to the target database's quoting and escaping
 * rules. Quoters can be used when executing {@link SQLBatchInsert batch inserts}. You
 * don't normally need to concern yourself with quoting and escaping unless you want to
 * {@link Transformer#transform(Object, Object, Quoter) tranform} a value into a
 * {@linkplain SQLExpression} SQL expression} before saving it to the database. SQL
 * expressions will be injected as-is into the INSERT statement. <i>Klojang JDBC</i> will
 * not attempt to "understand" them. That makes you responsible for protecting yourself
 * against SQL injection. Using a {@code Quoter} gives you just as much protection as
 * {@link java.sql.PreparedStatement prepared statements} as it completely outsources the
 * quoting and escaping to the JDBC driver.
 *
 * <p><b>Only use a {@code Quoter} to escape and quote strings <i>within</i> the SQL
 * expression</b>. In any other case escaping and quoting is taken care of by <i>Klojang
 * JDBC</i>, if not simply by JDBC itself.
 *
 * <p>Here is a clear (but not very useful) example of where and how you can use a
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
 *
 * @see SQLExpression
 * @see Statement#enquoteLiteral(String)
 * @see SQLBatchInsertBuilder#withTransformer(String, Transformer)
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
   *   <li>if the argument is an {@link SQL#expression(String) SQL expression}, a
   *   {@link Number} or a {@link Boolean}, this method returns {@code value.toString()}
   *   (in other words, it returns an <i>unquoted</i> string)
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
    } else if (value instanceof Number
          || value.getClass() == Boolean.class
          || value.getClass() == SQLException.class) {
      return value.toString();
    }
    return stmt.enquoteLiteral(value.toString());
  }
}
