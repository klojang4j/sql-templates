package org.klojang.jdbc.x.ps;

import org.klojang.jdbc.BindInfo;
import org.klojang.jdbc.x.sql.NamedParameter;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

/*
 * Binds the values within in a JavaBean to a  PreparedStatement.
 */
public final class BeanBinder<T> {

  private final List<NamedParameter> bound = new ArrayList<>();

  private final BeanValueBinder<?, ?>[] binders;

  // Creates a BeanBinder capable binding JavaBeans of the specified type to a query
  // containing the specified named parameters.
  public BeanBinder(Class<T> beanClass, List<NamedParameter> params, BindInfo bindInfo) {
    binders = BeanValueBinder.createBeanValueBinders(beanClass, params, bindInfo, bound);
  }

  public void bind(T bean, PreparedStatement ps) throws Throwable {
    BeanValueBinder.bindBean(ps, bean, binders);
  }

  // Returns all parameters in the query string that can be bound by this BeanBinder.
  public List<NamedParameter> getBoundParameters() {
    return List.copyOf(bound);
  }
}
