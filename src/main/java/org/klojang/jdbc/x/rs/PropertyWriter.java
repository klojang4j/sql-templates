package org.klojang.jdbc.x.rs;

import org.klojang.invoke.Setter;
import org.klojang.invoke.SetterFactory;
import org.klojang.jdbc.DatabaseException;
import org.klojang.jdbc.x.JDBC;
import org.klojang.templates.NameMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;

import static org.klojang.util.CollectionMethods.implode;

/**
 * Reads a single column in a ResultSet as the value for a bean property.
 *
 * @param <COLUMN_TYPE>
 * @param <FIELD_TYPE>
 */
public class PropertyWriter<COLUMN_TYPE, FIELD_TYPE> {

  private static final Logger LOG = LoggerFactory.getLogger(PropertyWriter.class);

  @SuppressWarnings("rawtypes")
  public static <U> U writeAll(ResultSet rs,
        Supplier<U> beanSupplier,
        PropertyWriter[] writers) throws Throwable {
    U bean = beanSupplier.get();
    for (PropertyWriter channel : writers) {
      channel.write(rs, bean);
    }
    return bean;
  }

  @SuppressWarnings("rawtypes")
  public static PropertyWriter[] createWriters(
        ResultSet rs, Class<?> beanClass, NameMapper nameMapper) {
    Map<String, Setter> setters = SetterFactory.INSTANCE.getSetters(beanClass);
    if (LOG.isTraceEnabled()) {
      log(beanClass, rs, setters);
    }
    ColumnReaderFactory negotiator = ColumnReaderFactory.getInstance();
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      int sz = rsmd.getColumnCount();
      List<PropertyWriter<?, ?>> writers = new ArrayList<>(sz);
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
        ColumnReader<?, ?> columnReader = negotiator.getReader(javaType, sqlType);
        writers.add(new PropertyWriter<>(columnReader, columnIndex, setter));
      }
      return writers.toArray(PropertyWriter[]::new);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private final Setter setter;
  private final ColumnReader<COLUMN_TYPE, FIELD_TYPE> reader;
  private final int columnIndex;

  private PropertyWriter(
        ColumnReader<COLUMN_TYPE, FIELD_TYPE> reader,
        int columnIndex,
        Setter setter) {
    this.reader = reader;
    this.columnIndex = columnIndex;
    this.setter = setter;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void write(ResultSet resultset, Object bean) throws Throwable {
    Class cls = setter.getParamType();
    Object val = reader.getValue(resultset, columnIndex, cls);
    LOG.trace("==> {}: {}", setter.getProperty(), val);
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
