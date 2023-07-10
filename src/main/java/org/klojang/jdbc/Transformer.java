package org.klojang.jdbc;

/**
 * Transformers enable you to modify values before they are saved to the database.
 *
 * @param <T> the type of the bean being saved to the database
 * @see SQLBatchInsertBuilder#withTransformer(String, Transformer)
 * @see SQLExpression
 * @see Quoter
 */
public interface Transformer<T> {

  /**
   * Transforms and returns the specified value.
   *
   * @param bean the bean containing the value
   * @param value the value to be transformed
   * @param quoter a {@link Quoter} instance that you can use if you intend to
   *     construct and return a {@link SQLExpression}. <b>Only use it for that particular
   *     case. Do not use it if you simply return a string value.</b>
   * @return the value to be saved to the database
   * @throws Throwable if an error occurs while transforming the value
   */
  Object transform(T bean, Object value, Quoter quoter) throws Throwable;

}
