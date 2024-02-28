package org.klojang.jdbc.x.ps;

import org.klojang.jdbc.SessionConfig;
import org.klojang.jdbc.x.sql.NamedParameter;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

/**
 * Binds the values within in a JavaBean or record to a PreparedStatement.
 */
public final class BeanBinder<T> {

  private final List<NamedParameter> bound = new ArrayList<>();

  private final PropertyBinder<?, ?>[] readers;

  // Creates a BeanBinder capable binding JavaBeans of the specified type to a query
  // containing the specified named parameters.
  public BeanBinder(Class<T> beanClass, List<NamedParameter> params, SessionConfig config) {
    readers = PropertyBinder.createReaders(beanClass, params, config, bound);
  }

  public void bind(PreparedStatement ps, T bean) throws Throwable {
    PropertyBinder.readAll(ps, bean, readers);
  }

  // Returns all parameters in the SQL that can be bound by this BeanBinder.
  public List<NamedParameter> getBoundParameters() {
    return List.copyOf(bound);
  }
}
