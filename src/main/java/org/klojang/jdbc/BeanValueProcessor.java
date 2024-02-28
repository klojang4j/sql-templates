package org.klojang.jdbc;

import java.util.List;

/**
 * Used to selectively modify values within a batch of JavaBeans or records. A
 * {@code BeanValueProcessor} can be "plugged into" batch inserts to apply last-minute
 * transformations on the beans or records, just before they are saved to the database.
 * Although it allows you to apply any transformations you like, its main purpose is to
 * enable you to generate {@linkplain SQLExpression SQL expressions} from bean values.
 * <i>Klojang JDBC</i> does not aim to be a SQL parser, and hence it does not know if and
 * how to escape the parts making up the expression. If you want to return a
 * {@link SQLExpression} from the
 * {@link #process(Object, String, Object, Quoter) process()} method, use the provided
 * {@link Quoter} to escape and quote strings <i>within</i> the expression. Do
 * <i>not</i> use a {@code BeanValueProcessor} just to return a quoted version of the
 * input value. <i>Klojang JDBC</i> already takes care of that. Every value that is saved
 * to the database will (again) go through
 * {@link Quoter#quoteValue(Object) Quoter.quoteValue()}, even it is the return value of
 * {@code BeanValueProcessor.process()}.
 *
 * @param <T> the type of the bean containing the value to be processed
 * @see Quoter
 * @see BatchInsertBuilder#withValueProcessor(BeanValueProcessor)
 */
@FunctionalInterface
public interface BeanValueProcessor<T> {

  /**
   * Returns a {@code BeanValueProcessor} that returns the input value as-is.
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
   * @param bean the bean or {@code record} containing the value to be converted
   * @param propertyName the name of the property or record component whose value to
   *       convert
   * @param propertyValue the value to be converted
   * @param quoter a {@code Quoter} to be used if you intend to return an
   *       {@link SQLExpression} containing string literals
   * @return the converted value
   */
  Object process(T bean, String propertyName, Object propertyValue, Quoter quoter);
}
