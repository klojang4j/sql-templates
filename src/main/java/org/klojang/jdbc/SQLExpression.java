package org.klojang.jdbc;

/**
 * A special string wrapper object whose type signals to <i>Klojang JDBC</i> that the
 * wrapped string must be treated as a native SQL expression and hence must not be escaped
 * or quoted. The SQL expression could be a SQL function call like
 * {@code CONCAT(FIRST_NAME, ' ' , LAST_NAME)}, or an operation like
 * {@code (SALARY * 0.05)}. Instances of {@code SQLExpression} are retrieved through
 * {@link SQL#expression(String) SQL.expression()}. SQL expressions will be injected as-is
 * into SQL statements. <i>Klojang JDBC</i> will not attempt to "understand" them. Use a
 * {@link Quoter} or {@link SQLSession#quoteValue(Object) SQLSession.quoteValue()} to
 * escape and quote the individual components of the expression if necessary.
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
