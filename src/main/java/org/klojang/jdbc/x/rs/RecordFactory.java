package org.klojang.jdbc.x.rs;

import org.klojang.jdbc.DatabaseException;
import org.klojang.jdbc.x.JDBC;
import org.klojang.templates.NameMapper;
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
import static org.klojang.util.CollectionMethods.implode;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class RecordFactory<T extends Record> {

  private record WriteConfig(MethodHandle constructor, ComponentWriter[] writers) { }

  private static final Logger LOG = LoggerFactory.getLogger(RecordFactory.class);

  private final MethodHandle constructor;
  private final ComponentWriter[] writers;

  public RecordFactory(Class<T> recordClass, ResultSet resultset, NameMapper mapper) {
    WriteConfig cfg = createWriters(recordClass, resultset, mapper);
    constructor = cfg.constructor();
    writers = cfg.writers();
  }

  @SuppressWarnings("unchecked")
  public T createRecord(ResultSet rs) throws Throwable {
    Object[] args = new Object[writers.length];
    for (int i = 0; i < writers.length; ++i) {
      Object val = writers[i].readValue(rs);
      LOG.trace("-> {}: {}: ", writers[i].component(), val);
      args[i] = val;
    }
    return (T) constructor.invokeWithArguments(args);
  }

  private static <T extends Record> WriteConfig createWriters(
        Class<T> recordClass,
        ResultSet resultset,
        NameMapper mapper) {
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
        String component = mapper.map(label);
        RecordComponent rc = components.get(component);
        if (rc == null) {
          String fmt = "Column {} cannot be mapped to a component of {}";
          LOG.warn(fmt, label, recordClass.getSimpleName());
          continue;
        }
        Class<?> type = rc.getType();
        ColumnReader reader = factory.getReader(type, sqlType);
        ComponentWriter writer = new ComponentWriter(reader, component, jdbcIdx, type);
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
