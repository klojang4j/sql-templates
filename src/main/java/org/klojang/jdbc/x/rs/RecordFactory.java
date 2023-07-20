package org.klojang.jdbc.x.rs;

import org.klojang.jdbc.x.JDBC;
import org.klojang.templates.NameMapper;
import org.klojang.util.CollectionMethods;
import org.klojang.util.ExceptionMethods;
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

  private record FactoryConfig(MethodHandle constructor, RecordChannel[] channels) {}

  private static final Logger LOG = LoggerFactory.getLogger(RecordFactory.class);

  private final MethodHandle constructor;
  private final RecordChannel[] channels;

  public RecordFactory(Class<T> recordClass, ResultSet resultset, NameMapper mapper) {
    FactoryConfig cfg = createChannels(recordClass, resultset, mapper);
    constructor = cfg.constructor();
    channels = cfg.channels();
  }

  @SuppressWarnings("unchecked")
  public T createRecord(ResultSet rs) throws Throwable {
    Object[] args = new Object[channels.length];
    for (int i = 0; i < channels.length; ++i) {
      args[i] = channels[i].readValue(rs);
    }
    return (T) constructor.invokeWithArguments(args);
  }


  private static <T extends Record> FactoryConfig createChannels(
        Class<T> recordClass,
        ResultSet resultset,
        NameMapper mapper) {
    Map<String, RecordComponent> components = getComponents(recordClass);
    ColumnReaderFinder negotiator = ColumnReaderFinder.getInstance();
    if (LOG.isTraceEnabled()) {
      log(resultset, recordClass, components);
    }
    List<Class<?>> paramTypes = new ArrayList<>(components.size());
    List<RecordChannel> channels = new ArrayList<>(components.size());
    try {
      ResultSetMetaData rsmd = resultset.getMetaData();
      for (int idx = 0; idx < rsmd.getColumnCount(); ++idx) {
        int jdbcIdx = idx + 1; // JDBC is one-based
        int sqlType = rsmd.getColumnType(jdbcIdx);
        String label = rsmd.getColumnLabel(jdbcIdx);
        String property = mapper.map(label);
        RecordComponent component = components.get(property);
        if (component == null) {
          String fmt = "Column {} cannot be mapped to a property of {}";
          LOG.warn(fmt, label, recordClass.getSimpleName());
          continue;
        }
        Class<?> javaType = component.getType();
        ColumnReader reader = negotiator.findReader(javaType, sqlType);
        RecordChannel channel = new RecordChannel(reader, jdbcIdx, javaType);
        paramTypes.add(javaType);
        channels.add(channel);
      }
      MethodHandle mh = publicLookup().findConstructor(
            recordClass,
            methodType(void.class, paramTypes.toArray(Class[]::new)));
      return new FactoryConfig(mh, channels.toArray(RecordChannel[]::new));
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
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
    LOG.trace("Properties ...: {}", implode(props));
  }

  private static Map<String, RecordComponent> getComponents(Class cls) {
    return CollectionMethods.collectionToMap(
          Arrays.asList(cls.getRecordComponents()),
          RecordComponent::getName);
  }

}
