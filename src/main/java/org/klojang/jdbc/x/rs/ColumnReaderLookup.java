package org.klojang.jdbc.x.rs;

import org.klojang.jdbc.x.rs.reader.*;

import java.util.HashMap;
import java.util.stream.IntStream;

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
public sealed class ColumnReaderLookup<T> extends HashMap<Integer, ColumnReader<?, ?>>
      permits BooleanReaderLookup,
      ByteReaderLookup,
      DoubleReaderLookup,
      EnumReaderLookup,
      FloatReaderLookup,
      IntReaderLookup,
      LocalDateReaderLookup,
      LocalDateTimeReaderLookup,
      LongReaderLookup,
      ShortReaderLookup,
      StringReaderLookup,
      UUIDReaderLookup {

  public ColumnReaderLookup() {
    super();
  }

  public final void add(int sqlType, ColumnReader<?, T> reader) {
    put(sqlType, reader);
  }

  public final void addMultiple(ColumnReader<?, T> reader, int... sqlTypes) {
    IntStream.of(sqlTypes).forEach(i -> put(i, reader));
  }

}
