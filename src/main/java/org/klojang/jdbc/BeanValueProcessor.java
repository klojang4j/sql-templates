package org.klojang.jdbc;

import java.util.List;

/**
 * Converts the value of a particular property within a particular bean instance. If a
 * {@link SQLExpression SQL expression} must be generated from the input value, the
 * provided {@link Quoter} can be used to escape and quote string literals <i>within</i>
 * the expression. <i>Do not use the {@code Quoter} to simply return a quoted version of
 * the input value.</i> The return value of the
 * {@link #process(Object, String, Object, Quoter) process()} method will always be
 * processed <i>again</i> by {@link Quoter#quoteValue(Object) Quoter.quoteValue()}. An
 * example of how to use a {@code BeanValueProcessor} is provided in the comments for
 * {@link SQLSession#setValues(List, BeanValueProcessor)}.
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
   * Converts the value of a bean property, potentially involving the specified
   * {@link Quoter} object.
   *
   * @param bean the bean or {@code record} containing the value
   * @param propertyName the name of the property or record component
   * @param propertyValue the value of the property or record component
   * @param quoter a {@code Quoter} to be used if you intend to return an
   *       {@link SQLExpression} containing strings of unknown origin
   * @return the converted value
   */
  Object process(T bean, String propertyName, Object propertyValue, Quoter quoter);
}
