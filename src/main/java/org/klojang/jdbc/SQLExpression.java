package org.klojang.jdbc;

/**
 * A {@code SQLExpression} is a simple string holder, but its type signals to <i>Klojang
 * JDBC</i> that the string it wraps must not be quoted or escaped. A
 * {@code SQLExpression} supposedly wraps a SQL expression like
 * {@code SUBSTRING('FooBar', 3, 3)}.
 *
 * @see SQL#expression(String)
 * @see Quoter
 * @see Transformer
 * @see SQLBatchInsertBuilder#withTransformer(String, Transformer)
 */
public final class SQLExpression {

  private final String expression;

  SQLExpression(String expression) {
    this.expression = expression;
  }

  /**
   * Returns the string wrapped by this instance.
   *
   * @return
   */
  @Override
  public String toString() { return expression; }
}
