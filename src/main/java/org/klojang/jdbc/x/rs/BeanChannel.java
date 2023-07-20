package org.klojang.jdbc.x.rs;

import org.klojang.invoke.Setter;
import org.klojang.invoke.SetterFactory;
import org.klojang.jdbc.x.JDBC;
import org.klojang.templates.NameMapper;
import org.klojang.util.ExceptionMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;

import static org.klojang.util.CollectionMethods.implode;

/* Transports a single value from a ResultSet to a bean */
public class BeanChannel<COLUMN_TYPE, FIELD_TYPE> {

  private static final Logger LOG = LoggerFactory.getLogger(BeanChannel.class);

  @SuppressWarnings("rawtypes")
  public static <U> U toBean(ResultSet rs,
        Supplier<U> beanSupplier,
        BeanChannel[] channels)
  throws Throwable {
    U bean = beanSupplier.get();
    for (BeanChannel channel : channels) {
      channel.copy(rs, bean);
    }
    return bean;
  }

  @SuppressWarnings("rawtypes")
  public static BeanChannel[] createChannels(
        ResultSet rs, Class<?> beanClass, NameMapper nameMapper) {
    Map<String, Setter> setters = SetterFactory.INSTANCE.getSetters(beanClass);
    if (LOG.isTraceEnabled()) {
      log(beanClass, rs, setters);
    }
    ColumnReaderFinder negotiator = ColumnReaderFinder.getInstance();
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      int sz = rsmd.getColumnCount();
      List<BeanChannel<?, ?>> channels = new ArrayList<>(sz);
      for (int idx = 0; idx < sz; ++idx) {
        int columnIndex = idx + 1; // JDBC is one-based
        int sqlType = rsmd.getColumnType(columnIndex);
        String label = rsmd.getColumnLabel(columnIndex);
        String property = nameMapper.map(label);
        Setter setter = setters.get(property);
        if (setter == null) {
          String fmt = "Column {} cannot be mapped to a property of {}";
          LOG.warn(fmt, label, beanClass.getSimpleName());
          continue;
        }
        Class<?> javaType = setter.getParamType();
        ColumnReader<?, ?> reader = negotiator.findReader(javaType, sqlType);
        channels.add(new BeanChannel<>(reader, columnIndex, setter));
      }
      return channels.toArray(BeanChannel[]::new);
    } catch (SQLException e) {
      throw ExceptionMethods.uncheck(e);
    }
  }

  private final ColumnReader<COLUMN_TYPE, FIELD_TYPE> reader;
  private final int columnIndex;
  private final Setter setter;

  private BeanChannel(
        ColumnReader<COLUMN_TYPE, FIELD_TYPE> reader,
        int columnIndex,
        Setter setter) {
    this.reader = reader;
    this.columnIndex = columnIndex;
    this.setter = setter;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void copy(ResultSet resultset, Object bean) throws Throwable {
    Class cls = setter.getParamType();
    Object val = reader.getValue(resultset, columnIndex, cls);
    setter.write(bean, val);
  }

  private static void log(
        Class<?> beanClass,
        ResultSet resultset,
        Map<String, Setter> setters) {
    LOG.trace("Mapping ResultSet to {}", beanClass.getSimpleName());
    Comparator<String> cmp = Comparator.comparing(String::toLowerCase);
    Set<String> cols = new TreeSet<>(cmp);
    cols.addAll(Arrays.asList(JDBC.getColumnNames(resultset)));
    Set<String> props = new TreeSet<>(cmp);
    props.addAll(setters.keySet());
    LOG.trace("Columns ......: {}", implode(cols));
    LOG.trace("Properties ...: {}", implode(props));
  }


}
