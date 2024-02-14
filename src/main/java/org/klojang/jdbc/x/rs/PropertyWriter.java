package org.klojang.jdbc.x.rs;

import org.klojang.invoke.Setter;
import org.klojang.invoke.SetterFactory;
import org.klojang.jdbc.DatabaseException;
import org.klojang.jdbc.SessionConfig;
import org.klojang.jdbc.x.JDBC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;

import static org.klojang.jdbc.SessionConfig.CustomReader;
import static org.klojang.util.CollectionMethods.implode;

/**
 * Reads a single column in a ResultSet as the value for a bean property.
 *
 * @param <COLUMN_TYPE>
 * @param <FIELD_TYPE>
 */
final class PropertyWriter<COLUMN_TYPE, FIELD_TYPE> {

  private static final Logger LOG = LoggerFactory.getLogger(PropertyWriter.class);

  @SuppressWarnings("rawtypes")
  static <U> U writeAll(ResultSet rs,
        Supplier<U> beanSupplier,
        PropertyWriter[] writers) throws Throwable {
    U bean = beanSupplier.get();
    for (PropertyWriter channel : writers) {
      channel.write(rs, bean);
    }
    return bean;
  }

  @SuppressWarnings("rawtypes")
  static PropertyWriter[] createWriters(
        ResultSet rs,
        Class<?> beanClass,
        SessionConfig config) {
    Map<String, Setter> setters = SetterFactory.INSTANCE.getSetters(beanClass);
    if (LOG.isTraceEnabled()) {
      log(beanClass, rs, setters);
    }
    ColumnReaderFactory factory = ColumnReaderFactory.getInstance();
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      int sz = rsmd.getColumnCount();
      List<PropertyWriter<?, ?>> writers = new ArrayList<>(sz);
      for (int idx = 0; idx < sz; ++idx) {
        int columnIndex = idx + 1; // JDBC is one-based
        int sqlType = rsmd.getColumnType(columnIndex);
        String label = rsmd.getColumnLabel(columnIndex);
        String property = config.getColumnToPropertyMapper().map(label);
        Setter setter = setters.get(property);
        if (setter == null) {
          if (LOG.isTraceEnabled()) {
            String fmt = "Column {} cannot be mapped to a property of {}";
            LOG.warn(fmt, label, beanClass.getSimpleName());
          }
          continue;
        }
        Class<?> javaType = setter.getParamType();
        CustomReader customReader = config.getCustomReader(beanClass,
              property,
              javaType,
              sqlType);
        PropertyWriter pw;
        if (customReader == null) {
          ColumnReader<?, ?> columnReader = factory.getReader(javaType, sqlType);
          pw = new PropertyWriter<>(setter, columnIndex, columnReader);
        } else {
          pw = new PropertyWriter(setter, columnIndex, customReader);
        }
        writers.add(pw);
      }
      return writers.toArray(PropertyWriter[]::new);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private final Setter setter;
  private final int columnIndex;
  private final ColumnReader<COLUMN_TYPE, FIELD_TYPE> reader;
  private final CustomReader custom;

  private PropertyWriter(Setter setter,
        int columnIndex,
        ColumnReader<COLUMN_TYPE, FIELD_TYPE> reader) {
    this.setter = setter;
    this.columnIndex = columnIndex;
    this.reader = reader;
    this.custom = null;
  }

  private PropertyWriter(Setter setter, int columnIndex, CustomReader custom) {
    this.setter = setter;
    this.columnIndex = columnIndex;
    this.custom = custom;
    this.reader = null;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void write(ResultSet resultset, Object bean) throws Throwable {
    final Object val;
    if (custom == null) {
      Class cls = setter.getParamType();
      val = reader.getValue(resultset, columnIndex, cls);
      if (LOG.isTraceEnabled()) {
        LOG.trace("==> {}: {}", setter.getProperty(), val);
      }
    } else {
      val = custom.getValue(resultset, columnIndex);
      if (LOG.isTraceEnabled()) {
        LOG.trace("==> {}: {} (using custom reader)", setter.getProperty(), val);
      }
    }
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
    if (LOG.isTraceEnabled()) {
      LOG.trace("Columns ......: {}", implode(cols));
      if (beanClass.isRecord()) {
        LOG.trace("Components ...: {}", implode(props));
      } else {
        LOG.trace("Properties ...: {}", implode(props));
      }
    }
  }


}
