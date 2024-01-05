package org.klojang.jdbc.x.ps;

import org.klojang.invoke.Getter;
import org.klojang.invoke.GetterFactory;
import org.klojang.jdbc.BindInfo;
import org.klojang.jdbc.x.ps.writer.EnumWriterLookup;
import org.klojang.jdbc.x.sql.NamedParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.util.*;

import static org.klojang.util.ClassMethods.isSubtype;


/**
 * Binds a bean property (its value) into a PreparedStatement.
 *
 * @param <FIELD_TYPE> the type of the bean property
 * @param <PARAM_TYPE> the type of the value passed to
 *       PreparedStatement.setXXX(parameterIndex, value)
 * @author Ayco Holleman
 */
final class BeanPropertyBinder<FIELD_TYPE, PARAM_TYPE> {

  private static final Logger LOG = LoggerFactory.getLogger(BeanPropertyBinder.class);

  @SuppressWarnings({"rawtypes", "unchecked"})
  static <T> void bindBean(PreparedStatement ps, T bean, BeanPropertyBinder[] binders)
        throws Throwable {
    LOG.debug("Binding {} to PreparedStatement", bean.getClass().getSimpleName());
    for (BeanPropertyBinder binder : binders) {
      binder.bindProperty(ps, bean);
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  static BeanPropertyBinder[] createPropertyBinders(Class beanClass,
        List<NamedParameter> params,
        BindInfo bindInfo,
        List<NamedParameter> bound) {
    ColumnWriterFinder finder = ColumnWriterFinder.getInstance();
    Map<String, Getter> getters = GetterFactory.INSTANCE.getGetters(beanClass, true);
    List<BeanPropertyBinder> binders = new ArrayList<>(params.size());
    for (NamedParameter param : params) {
      Getter getter = getters.get(param.name());
      if (getter == null) {
        continue;
      }
      bound.add(param);
      String property = param.name();
      Class type = getter.getReturnType();
      ColumnWriter writer;
      if (isSubtype(type, Enum.class) && bindInfo.saveEnumAsString(beanClass, property)) {
        writer = EnumWriterLookup.ENUM_TO_STRING;
      } else {
        Integer sqlType = bindInfo.getSqlType(beanClass, property, type);
        if (sqlType == null) {
          writer = finder.getDefaultWriter(type);
        } else {
          writer = finder.findWriter(type, sqlType);
        }
      }
      binders.add(new BeanPropertyBinder(getter, writer, param));
    }
    return binders.toArray(BeanPropertyBinder[]::new);
  }

  private final Getter getter;
  private final ColumnWriter<FIELD_TYPE, PARAM_TYPE> writer;
  private final NamedParameter param;

  private BeanPropertyBinder(Getter getter,
        ColumnWriter<FIELD_TYPE, PARAM_TYPE> writer,
        NamedParameter param) {
    this.getter = getter;
    this.writer = writer;
    this.param = param;
  }

  @SuppressWarnings("unchecked")
  private <T> void bindProperty(PreparedStatement ps, T bean) throws Throwable {
    FIELD_TYPE beanValue = (FIELD_TYPE) getter.read(bean);
    PARAM_TYPE paramValue = writer.getParamValue(beanValue);
    if (beanValue == paramValue) {
      // Reference equality indicates that the bean value was bound as-is
      // into the PreparedStatement - using its setXXX() methods. Otherwise
      // the value was first transformed using a Adapter.
      LOG.trace("-> Parameter \"{}\": {}", getter.getProperty(), paramValue);
    } else {
      String fmt = "-> Parameter \"{}\": {} (bean value: {})";
      LOG.trace(fmt, param.name(), paramValue, beanValue);
    }
    param.positions().forEachThrowing(i -> writer.bind(ps, i, paramValue));
  }

}
