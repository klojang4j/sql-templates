package org.klojang.jdbc;

/**
 * Transformers enable you to modify values before they are saved to the database.
 * Transformers can be configured for bean properties while preparing a batch INSERT using
 * an {@link SQLBatchInsertBuilder}.
 *
 * @param <T> the type of the bean being saved to the database
 * @see SQLBatchInsertBuilder#withTransformer(String, Transformer)
 * @see SQL#expression(String)
 * @see Quoter
 */
public interface Transformer<T> {

  /**
   * Transforms and returns the specified value.
   *
   * @param bean the bean containing the value
   * @param value the value to be transformed
   * @param quoter a {@link Quoter} instance that you can use if you intend to construct
   * and return a {@link SQLExpression}. If you return anything else, don't use the
   * quoter. Escaping and quoting will be taken care of by <i>Klojang JDBC</i> or simply
   * by JDBC itself. Only use the {@code Quoter} to escape and quote strings <i>within</i>
   * an SQL expression.
   * @return the value to be saved to the database
   * @throws Throwable if an error occurs while transforming the value
   */
  Object transform(T bean, Object value, Quoter quoter) throws Throwable;

}