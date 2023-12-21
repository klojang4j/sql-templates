package org.klojang.jdbc.x.ps;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.klojang.jdbc.BindInfo;
import org.klojang.jdbc.x.ps.writer.EnumWriterLookup;
import org.klojang.jdbc.x.sql.NamedParameter;
import org.klojang.invoke.Getter;
import org.klojang.invoke.GetterFactory;
import org.klojang.util.ClassMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Binds a single value from a JavaBean into a PreparedStatement.
 *
 * @param <FIELD_TYPE> the type of the bean property
 * @param <PARAM_TYPE>
 * @author Ayco Holleman
 */
class BeanValueBinder<FIELD_TYPE, PARAM_TYPE> {

  private static final Logger LOG = LoggerFactory.getLogger(BeanValueBinder.class);

  static <T> void bindBean(PreparedStatement ps,
      T bean,
      BeanValueBinder<?, ?>[] binders) throws Throwable {
    LOG.debug("Binding {} to PreparedStatement", bean.getClass().getSimpleName());
    for (BeanValueBinder<?, ?> binder : binders) {
      binder.bindValue(ps, bean);
    }
  }

  static BeanValueBinder<?, ?>[] createBeanValueBinders(Class<?> beanClass,
      List<NamedParameter> params,
      BindInfo bindInfo,
      Collection<NamedParameter> bound) {
    ColumnWriterFinder negotiator = ColumnWriterFinder.getInstance();
    Map<String, Getter> getters = GetterFactory.INSTANCE.getGetters(beanClass, true);
    List<BeanValueBinder<?, ?>> binders = new ArrayList<>(params.size());
    for (NamedParameter param : params) {
      Getter getter = getters.get(param.name());
      if (getter == null) {
        continue;
      }
      bound.add(param);
      String property = param.name();
      Class<?> type = getter.getReturnType();
      ColumnWriter<?, ?> writer;
      if (ClassMethods.isSubtype(type, Enum.class)
          && bindInfo.saveEnumAsString(beanClass, property)) {
        writer = EnumWriterLookup.ENUM_TO_STRING;
      } else {
        Integer sqlType = bindInfo.getSqlType(beanClass, property, type);
        if (sqlType == null) {
          writer = negotiator.getDefaultWriter(type);
        } else {
          writer = negotiator.findWriter(type, sqlType);
        }
      }
      binders.add(new BeanValueBinder<>(getter, writer, param));
    }
    return binders.toArray(BeanValueBinder[]::new);
  }

  private final Getter getter;
  private final ColumnWriter<FIELD_TYPE, PARAM_TYPE> writer;
  private final NamedParameter param;

  private BeanValueBinder(Getter getter,
      ColumnWriter<FIELD_TYPE, PARAM_TYPE> writer,
      NamedParameter param) {
    this.getter = getter;
    this.writer = writer;
    this.param = param;
  }

  @SuppressWarnings("unchecked")
  private <T> void bindValue(PreparedStatement ps, T bean) throws Throwable {
    FIELD_TYPE beanValue = (FIELD_TYPE) getter.read(bean);
    PARAM_TYPE paramValue = writer.getParamValue(beanValue);
    if (beanValue == paramValue) { // No adapter defined
      LOG.trace("-> Parameter \"{}\": {}", getter.getProperty(), paramValue);
    } else {
      String fmt = "-> Parameter \"{}\": {} (bean value: {})";
      LOG.trace(fmt, param.name(), paramValue, beanValue);
    }
    param.positions().forEachThrowing(i -> writer.bind(ps, i, paramValue));
  }

}
