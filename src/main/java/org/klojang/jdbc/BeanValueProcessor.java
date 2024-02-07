package org.klojang.jdbc;

import java.util.List;

/**
 * Used to selectively modify values within a batch of JavaBeans or records. A
 * {@code BeanValueProcessor} can be specified for batch inserts to apply last-minute
 * transformations on the beans or records before they are saved to the database. Although
 * you can apply any transformations you like, it is mainly there for a rather special and
 * specific purpose: to replace bean values with
 * {@linkplain SQLExpression SQL expressions}.
 * <i>Klojang JDBC</i> does not aim to be a SQL parser, and hence it does not know if and
 * how to escape the parts making up the expression. If you want to return a
 * {@link SQLExpression} from
 * {@link #process(Object, String, Object, Quoter) BeanValueProcessor.process()}, you can
 * use the provided {@link Quoter} to escape and quote strings <b>within</b> the
 * expression. Do not use a {@code BeanValueProcessor} just to return a quoted version of
 * the input value. <i>Klojang JDBC</i> already takes care of that. Each and every value
 * that is going to be saved to the database will always be processed by
 * {@link Quoter#quoteValue(Object) Quoter.quoteValue()}, even it is the return value of
 * {@code BeanValueProcessor.process()}. See
 * {@link SQLSession#setValues(List, BeanValueProcessor)} for an example of how to use a
 * {@code BeanValueProcessor}.
 *
 * @param <T> the type of the bean containing the value to be processed
 * @see Quoter
 * @see SQLSession#setValues(List, BeanValueProcessor)
 * @see BatchInsertBuilder#withValueProcessor(BeanValueProcessor)
 */
@FunctionalInterface
public interface BeanValueProcessor<T> {

  /**
   * Returns a {@code BeanValueProcessor} that returns any value passed to it as-is.
   *
   * @param <U> the type of the bean or record containing the value
   * @return a {@code BeanValueProcessor} that returns any value passed to it as-is
   * @see SQLSession#quoteValue(Object)
   */
  static <U> BeanValueProcessor<U> identity() {
    return (bean, prop, val, quoter) -> val;
  }

  /**
   * Converts the value of a bean property or record component, potentially involving the
   * specified {@link Quoter} object.
   *
   * @param bean the bean or {@code record} containing the value
   * @param propertyName the name of the property or record component
   * @param propertyValue the value of the specified property within the specified
   *       bean
   * @param quoter a {@code Quoter} to be used if you intend to return an
   *       {@link SQLExpression} containing string literals
   * @return the converted value
   */
  Object process(T bean, String propertyName, Object propertyValue, Quoter quoter);
}
