package org.klojang.jdbc;

/**
 * A special string wrapper object that functions as a signal to <i>Klojang JDBC</i> that
 * the wrapped string must be treated as a native SQL expression and hence not escaped or
 * quoted. The SQL expression could be a SQL function call like
 * {@code CONCAT(FIRST_NAME, ' ' , LAST_NAME)}, or operations like
 * {@code (SALARY * 0.05)}. Instances of {@code SQLExpression} are retrieved through
 * {@link SQL#expression(String) SQL.expression()}. SQL expressions will be injected as-is
 * into SQL statements. <i>Klojang JDBC</i> will not attempt to "understand" them. Use a
 * {@link Quoter} to escape the individual components of the expression.
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
  public String toString() {return expression;}
}
