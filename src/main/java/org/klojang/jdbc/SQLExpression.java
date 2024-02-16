package org.klojang.jdbc;

/**
 * <p>A special string wrapper object whose type signals to <i>Klojang JDBC</i> that the
 * wrapped string must be treated as a native SQL expression and hence must not be escaped
 * or quoted. The SQL expression could be, for example:
 * <ul>
 *   <li>a SQL function call like <b>{@code CONCAT(FIRST_NAME, ' ' , LAST_NAME)}</b>
 *   <li>an operation like <b>{@code (SALARY * 1.05)}</b>
 *   <li>a number constant like <b>{@code 42}</b>
 *   <li>an already properly escaped and quoted string literal like
 *       <b>{@code 'Chicago O''Hare International Airport'}</b>
 * </ul>
 * <p>SQL expressions will be injected as-is into SQL statements. Use a {@link Quoter} or
 * the various {@code quote} methods in {@link SQLSession} to escape and quote the
 * individual components of the expression if necessary.
 *
 * <p><i>Although you could, it is unnecessary to wrap a plain number into an
 * {@code SQLExpression}. Numbers (primitive numbers or instances of {@link Number})
 * will anyhow not be quoted by Klojang JDBC.</i>
 */
public final class SQLExpression {

  private final String expression;

  SQLExpression(String expression) {
    this.expression = expression;
  }

  /**
   * Returns the SQL expression.
   *
   * @return the SQL expression
   */
  @Override
  public String toString() { return expression; }
}
