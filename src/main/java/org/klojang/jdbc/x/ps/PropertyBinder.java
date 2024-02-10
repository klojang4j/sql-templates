package org.klojang.jdbc.x.ps;

import org.klojang.invoke.Getter;
import org.klojang.invoke.GetterFactory;
import org.klojang.jdbc.BindInfo;
import org.klojang.jdbc.x.ps.writer.EnumBinderLookup;
import org.klojang.jdbc.x.sql.NamedParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.klojang.util.ClassMethods.isSubtype;


/**
 * Binds a single bean property or record component to a PreparedStatement.
 *
 * @param <INPUT_TYPE> the type of the bean property
 * @param <PARAM_TYPE> the type of the value passed to
 *       PreparedStatement.setXXX(parameterIndex, value)
 * @author Ayco Holleman
 */
final class PropertyBinder<INPUT_TYPE, PARAM_TYPE> {

  private static final Logger LOG = LoggerFactory.getLogger(PropertyBinder.class);

  @SuppressWarnings({"rawtypes", "unchecked"})
  static <T> void readAll(PreparedStatement ps, T bean, PropertyBinder[] readers)
        throws Throwable {
    LOG.debug("Binding {} to PreparedStatement", bean.getClass().getSimpleName());
    for (PropertyBinder reader : readers) {
      reader.bindProperty(ps, bean);
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  static PropertyBinder[] createReaders(Class beanClass,
        List<NamedParameter> params,
        BindInfo bindInfo,
        List<NamedParameter> bound) {
    ColumnWriterFactory factory = ColumnWriterFactory.getInstance();
    Map<String, Getter> getters = GetterFactory.INSTANCE.getGetters(beanClass, true);
    List<PropertyBinder> readers = new ArrayList<>(params.size());
    for (NamedParameter param : params) {
      Getter getter = getters.get(param.name());
      if (getter == null) {
        continue;
      }
      bound.add(param);
      String property = param.name();
      Class type = getter.getReturnType();
      ValueBinder writer;
      if (isSubtype(type, Enum.class) && bindInfo.saveEnumAsString(beanClass, property)) {
        writer = EnumBinderLookup.ENUM_TO_STRING;
      } else {
        Integer sqlType = bindInfo.getSqlType(beanClass, property, type);
        if (sqlType == null) {
          writer = factory.getDefaultWriter(type);
        } else {
          writer = factory.getWriter(type, sqlType);
        }
      }
      readers.add(new PropertyBinder(getter, writer, param));
    }
    return readers.toArray(PropertyBinder[]::new);
  }

  private final Getter getter;
  private final ValueBinder<INPUT_TYPE, PARAM_TYPE> binder;
  private final NamedParameter param;

  private PropertyBinder(Getter getter,
        ValueBinder<INPUT_TYPE, PARAM_TYPE> binder,
        NamedParameter param) {
    this.getter = getter;
    this.binder = binder;
    this.param = param;
  }

  @SuppressWarnings("unchecked")
  private <T> void bindProperty(PreparedStatement ps, T bean) throws Throwable {
    INPUT_TYPE beanValue = (INPUT_TYPE) getter.read(bean);
    PARAM_TYPE paramValue = binder.getParamValue(beanValue);
    if (LOG.isTraceEnabled()) {
      if (binder.isAdaptive() && beanValue != paramValue) {
        String fmt = "==> Parameter \"{}\": {} (bean value: {})";
        LOG.trace(fmt, param.name(), paramValue, beanValue);
      } else {
        LOG.trace("==> Parameter \"{}\": {}", getter.getProperty(), paramValue);
      }
    }
    param.positions().forEachThrowing(i -> binder.bind(ps, i, paramValue));
  }

}
