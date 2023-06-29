package org.klojang.jdbc.x.rs;

import org.klojang.check.Check;
import org.klojang.collections.TypeMap;
import org.klojang.jdbc.x.rs.reader.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.klojang.check.CommonChecks.notNull;
import static org.klojang.jdbc.SQLTypeNames.getTypeName;
import static org.klojang.util.ClassMethods.className;

/*
 * Finds the most suitable of the ResultSet.getXXX() methods for a given Java type. If no
 * sure-fire match can be found, then an Adapter function can be specified that converts
 * the result of the getXXX() method to the required Java type. Therefore what actually
 * gets negotiated is not so much a getXXX() method per se, but a ResultSetReader, which
 * combines a getXXX() method with an (optional) converter function.
 */
@SuppressWarnings("rawtypes")
public class ColumnReaderFinder {

  private static ColumnReaderFinder INSTANCE;

  public static ColumnReaderFinder getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ColumnReaderFinder();
    }
    return INSTANCE;
  }

  private final Map<Class<?>, Map<Integer, ColumnReader>> all;

  private ColumnReaderFinder() {
    all = configure();
  }

  @SuppressWarnings({"unchecked"})
  public <T, U> ColumnReader<T, U> findReader(Class<U> fieldType, int sqlType) {
    // Implicitly checks that the specified int is one of the
    // static final int constants in java.sql.Types
    String sqlTypeName = getTypeName(sqlType);
    Map<Integer, ColumnReader> extractors = Check.that(all.get(fieldType))
          .is(notNull(), "type not supported: ${0}", className(fieldType))
          .ok();
    return Check.that(extractors.get(sqlType))
          .is(notNull(), "cannot convert ${0} to ${1}", sqlTypeName, className(fieldType))
          .ok();
  }

  private static Map configure() {
    return TypeMap.nativeTypeMapBuilder()
          .autobox(true)
          .add(String.class, immutable(new StringReaderLookup()))
          .add(Integer.class, immutable(new IntReaderLookup()))
          .add(Double.class, immutable(new DoubleReaderLookup()))
          .add(Long.class, immutable(new LongReaderLookup()))
          .add(Float.class, immutable(new FloatReaderLookup()))
          .add(Short.class, immutable(new ShortReaderLookup()))
          .add(Byte.class, immutable(new ByteReaderLookup()))
          .add(Boolean.class, immutable(new BooleanReaderLookup()))
          .add(LocalDate.class, immutable(new LocalDateReaderLookup()))
          .add(LocalDateTime.class, immutable(new LocalDateTimeReaderLookup()))
          .add(Enum.class, immutable(new EnumReaderLookup()))
          .freeze();
  }

  private static Map<Integer, ColumnReader> immutable(ColumnReaderLookup<?> src) {
    return Map.copyOf(src);
  }

}
