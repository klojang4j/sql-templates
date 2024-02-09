package org.klojang.jdbc.x.rs.reader;

import org.klojang.jdbc.x.rs.Adapter;
import org.klojang.jdbc.x.rs.ColumnReader;
import org.klojang.jdbc.x.rs.ColumnReaderLookup;
import org.klojang.jdbc.x.rs.ResultSetMethod;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.Map.Entry;
import static java.util.stream.Collectors.toList;

/**
 * Maps column types to a given Java type. The Java type is specified, in subclasses of
 * ColumnReaderLookup, through type parameter {@code <T>}. More specifically, this class
 * determines to most suitable {@code ResultSet.getXXX()} for producing a value of the
 * requested target type (which is a Java type). Obviously, if the column type is INTEGER
 * and the target type is {@code int} or {@code Integer}, that would be
 * {@code ResultSet.getInt()}. If the column type is SMALLINT or TINYINT, it would
 * <i>still</i> be {@code ResultSet.getInt()}, even though {@code ResultSet} has methods
 * like {@code getShort()} and {@code getByte()}! After all, what we want is an
 * {@code int} value, and the JDBC driver will not have a problem applying
 * {@code getInt()} to a SMALLINT column - that's a simple widening conversion. However,
 * if the column type would be NUMBER or VARCHAR, we need to pick some {@code getXXX()}
 * method, and post-process its result with a custom conversion function. This combination
 * of a {@code getXXX()} method and a custom conversion function is what is encapsulated
 * by the {@code ColumnReader} class.
 *
 * The map keys are supposed to be integer constants from
 * {@link java.sql.Types java.sql.Types} (e.g. {@code Types.INTEGER} or
 * {@code Types.VARCHAR}).
 *
 * @param <T> the Java type to which we want to convert the column type
 */
public abstract sealed class AbstractColumnReaderLookup<T>
      implements ColumnReaderLookup<T>
      permits
      BigDecimalReaderLookup,
      BooleanReaderLookup,
      ByteReaderLookup,
      DoubleReaderLookup,
      EnumReaderLookup,
      FloatReaderLookup,
      IntReaderLookup,
      LocalDateReaderLookup,
      LocalDateTimeReaderLookup,
      LongReaderLookup,
      ShortReaderLookup,
      UUIDReaderLookup {

  private final Map<Integer, ColumnReader<?, T>> readers;

  @SuppressWarnings("unchecked")
  AbstractColumnReaderLookup() {
    readers = Map.ofEntries(getColumnReaders().toArray(Entry[]::new));
  }

  public ColumnReader<?, T> getColumnReader(int columnType) {
    return readers.get(columnType);
  }

  abstract List<Entry<Integer, ColumnReader<?, T>>> getColumnReaders();

  final List<Entry<Integer, ColumnReader<?, T>>> entries(ResultSetMethod<?> method,
        int... columnTypes) {
    return IntStream.of(columnTypes)
          .mapToObj(i -> entry(method, i))
          .collect(toList());
  }

  final List<Entry<Integer, ColumnReader<?, T>>> entries(ResultSetMethod<?> method,
        Function<?, T> adapter,
        int... columnTypes) {
    return IntStream.of(columnTypes)
          .mapToObj(i -> entry(method, adapter, i))
          .collect(toList());
  }

  final List<Entry<Integer, ColumnReader<?, T>>> entries(ResultSetMethod<?> method,
        Adapter<?, T> adapter,
        int... columnTypes) {
    return IntStream.of(columnTypes)
          .mapToObj(i -> entry(method, adapter, i))
          .collect(toList());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  final Entry<Integer, ColumnReader<?, T>> entry(ResultSetMethod<?> method,
        int columnType) {
    return Map.entry(columnType, new ColumnReader(method));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  final Entry<Integer, ColumnReader<?, T>> entry(ResultSetMethod<?> method,
        Function<?, T> adapter,
        int columnType) {
    return Map.entry(columnType, new ColumnReader(method, adapter));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  Entry<Integer, ColumnReader<?, T>> entry(ResultSetMethod<?> method,
        Adapter<?, T> adapter,
        int columnType) {
    return Map.entry(columnType, new ColumnReader(method, adapter));
  }


}
