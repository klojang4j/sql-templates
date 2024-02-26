package org.klojang.jdbc.x.rs;

import org.klojang.jdbc.DatabaseException;
import org.klojang.jdbc.SessionConfig;
import org.klojang.jdbc.x.JDBC;
import org.klojang.util.CollectionMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.RecordComponent;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;

import org.klojang.jdbc.CustomReader;
import static org.klojang.util.CollectionMethods.implode;


@SuppressWarnings({"rawtypes", "unchecked"})
public final class RecordFactory<T extends Record> {

  private record WriteConfig(MethodHandle constructor, ComponentWriter[] writers) { }

  private static final Logger LOG = LoggerFactory.getLogger(RecordFactory.class);

  private final MethodHandle constructor;
  private final ComponentWriter[] writers;

  public RecordFactory(Class<T> recordClass, ResultSet resultset, SessionConfig config) {
    WriteConfig cfg = createWriters(recordClass, resultset, config);
    constructor = cfg.constructor();
    writers = cfg.writers();
  }

  @SuppressWarnings("unchecked")
  public T createRecord(ResultSet rs) throws Throwable {
    Object[] args = new Object[writers.length];
    for (int i = 0; i < writers.length; ++i) {
      args[i] = writers[i].readValue(rs);
    }
    return (T) constructor.invokeWithArguments(args);
  }

  private static <T extends Record> WriteConfig createWriters(
        Class<T> recordClass,
        ResultSet resultset,
        SessionConfig config) {
    Map<String, RecordComponent> components = getComponents(recordClass);
    ColumnReaderFactory factory = ColumnReaderFactory.getInstance();
    if (LOG.isTraceEnabled()) {
      log(resultset, recordClass, components);
    }
    List<Class<?>> paramTypes = new ArrayList<>(components.size());
    List<ComponentWriter> writers = new ArrayList<>(components.size());
    try {
      ResultSetMetaData rsmd = resultset.getMetaData();
      for (int idx = 0; idx < rsmd.getColumnCount(); ++idx) {
        int jdbcIdx = idx + 1; // JDBC is one-based
        int sqlType = rsmd.getColumnType(jdbcIdx);
        String label = rsmd.getColumnLabel(jdbcIdx);
        // the name of the record component
        String componentName = config.getColumnToPropertyMapper().map(label);
        RecordComponent component = components.get(componentName);
        if (component == null) {
          if (LOG.isTraceEnabled()) {
            String fmt = "Column {} cannot be mapped to a component of {}";
            LOG.warn(fmt, label, recordClass.getSimpleName());
          }
          continue;
        }
        Class<?> type = component.getType();
        CustomReader customReader = config.getCustomReader(recordClass,
              componentName,
              type,
              sqlType);
        ComponentWriter writer;
        if (customReader == null) {
          ColumnReader reader = factory.getReader(type, sqlType);
          writer = new ComponentWriter(reader, componentName, jdbcIdx, type);
        } else {
          writer = new ComponentWriter(customReader, componentName, jdbcIdx);
        }
        paramTypes.add(type);
        writers.add(writer);
      }
      MethodHandle mh = publicLookup().findConstructor(
            recordClass,
            methodType(void.class, paramTypes.toArray(Class[]::new)));
      return new WriteConfig(mh, writers.toArray(ComponentWriter[]::new));
    } catch (Throwable t) {
      throw new DatabaseException(t);
    }
  }

  private static <T extends Record> void log(ResultSet rs,
        Class<T> recordClass,
        Map<String, RecordComponent> components) {
    LOG.trace("Mapping ResultSet to {}", recordClass.getSimpleName());
    Comparator<String> cmp = Comparator.comparing(String::toLowerCase);
    Set<String> cols = new TreeSet<>(cmp);
    cols.addAll(Arrays.asList(JDBC.getColumnNames(rs)));
    Set<String> props = new TreeSet<>(cmp);
    props.addAll(components.keySet());
    LOG.trace("Columns ......: {}", implode(cols));
    LOG.trace("Components ...: {}", implode(props));
  }

  private static Map<String, RecordComponent> getComponents(Class cls) {
    return CollectionMethods.collectionToMap(
          Arrays.asList(cls.getRecordComponents()),
          RecordComponent::getName);
  }

}
