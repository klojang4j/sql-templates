package org.klojang.jdbc.x.ps;

import org.klojang.invoke.Getter;
import org.klojang.invoke.GetterFactory;
import org.klojang.jdbc.SessionConfig;
import org.klojang.jdbc.x.ps.writer.EnumBinderLookup;
import org.klojang.jdbc.x.sql.NamedParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.klojang.jdbc.SessionConfig.CustomBinder;
import static org.klojang.util.ClassMethods.isSubtype;


/**
 * Binds a single bean property or record component to a PreparedStatement.
 *
 * @param <INPUT_TYPE> the type of the value to be bound
 * @param <PARAM_TYPE> the type of the value passed to the particular
 *       {@code setXXX()} method of {@code PreparedStatement} that we want to use
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
        SessionConfig config,
        List<NamedParameter> bound) {
    ValueBinderFactory factory = ValueBinderFactory.getInstance();
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
      CustomBinder custom = config.getCustomBinder(beanClass, property, type);
      if (custom != null) {
        PropertyBinder pb = new PropertyBinder(getter, param, custom);
        readers.add(pb);
      } else {
        final ValueBinder vb;
        Integer sqlType = config.getSqlType(beanClass, property, type);
        if (sqlType == null) {
          if (isSubtype(type, Enum.class)) {
            if (config.saveEnumAsString(beanClass, property, type)) {
              vb = ValueBinder.ANY_TO_STRING;
            } else {
              vb = EnumBinderLookup.DEFAULT;
            }
          } else {
            vb = factory.getDefaultBinder(type);
          }
        } else {
          vb = factory.getBinder(type, sqlType);
        }
        readers.add(new PropertyBinder(getter, param, vb));
      }
    }
    return readers.toArray(PropertyBinder[]::new);
  }

  private final Getter getter;
  private final ValueBinder<INPUT_TYPE, PARAM_TYPE> binder;
  private final NamedParameter param;
  private final CustomBinder custom;

  private PropertyBinder(Getter getter,
        NamedParameter param, ValueBinder<INPUT_TYPE, PARAM_TYPE> binder) {
    this.getter = getter;
    this.param = param;
    this.binder = binder;
    this.custom = null;
  }

  private PropertyBinder(Getter getter, NamedParameter param, CustomBinder custom) {
    this.getter = getter;
    this.param = param;
    this.custom = custom;
    this.binder = null;
  }

  @SuppressWarnings("unchecked")
  private <T> void bindProperty(PreparedStatement ps, T bean) throws Throwable {
    INPUT_TYPE beanValue = (INPUT_TYPE) getter.read(bean);
    if (custom != null) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("==> Parameter \"{}\": {} (using custom binder)",
              param.name(),
              beanValue);
      }
      param.positions().forEachThrowing(i -> custom.bind(ps, i, beanValue));
    } else {
      PARAM_TYPE paramValue = binder.getParamValue(beanValue);
      if (LOG.isTraceEnabled()) {
        if (binder.isAdaptive() && beanValue != paramValue) {
          String fmt = "==> Parameter \"{}\": {} (original value: {})";
          LOG.trace(fmt, param.name(), paramValue, beanValue);
        } else {
          LOG.trace("==> Parameter \"{}\": {}", getter.getProperty(), paramValue);
        }
      }
      param.positions().forEachThrowing(i -> binder.bind(ps, i, paramValue));
    }
  }

}
