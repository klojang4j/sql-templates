package org.klojang.jdbc;

/**
 * A special string wrapper object whose type signals to <i>Klojang JDBC</i> that the
 * wrapped string must be treated as a native SQL expression and hence must not be escaped
 * or quoted. The SQL expression could be, for example:
 * <ul>
 *   <li>a SQL function call like <b>{@code CONCAT(FIRST_NAME, ' ' , LAST_NAME)}</b>
 *   <li>an operation like <b>{@code (SALARY * 1.05)}</b>
 *   <li>a number constant like <b>{@code 42}</b>
 *   <li>an already properly escaped and quoted string literal like
 *       <b>{@code 'Chicago O''Hare International Airport'}</b>
 * </ul>
 * SQL expressions will be injected as-is into SQL statements. Use a {@link Quoter} or
 * {@link SQLSession#quoteValue(Object) SQLSession.quoteValue()} to escape and quote the
 * individual components of the expression if necessary.
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
