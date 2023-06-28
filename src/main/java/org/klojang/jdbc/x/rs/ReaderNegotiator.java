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
public class ReaderNegotiator {

  private static ReaderNegotiator INSTANCE;

  public static ReaderNegotiator getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ReaderNegotiator();
    }
    return INSTANCE;
  }

  private final Map<Class<?>, Map<Integer, ResultSetReader>> all;

  private ReaderNegotiator() {
    all = configure();
  }

  @SuppressWarnings({"unchecked"})
  public <T, U> ResultSetReader<T, U> findReader(Class<U> fieldType, int sqlType) {
    // Implicitly checks that the specified int is one of the
    // static final int constants in java.sql.Types
    String sqlTypeName = getTypeName(sqlType);
    Map<Integer, ResultSetReader> extractors = Check.that(all.get(fieldType))
          .is(notNull(), "type not supported: ${0}", className(fieldType))
          .ok();
    return Check.that(extractors.get(sqlType))
          .is(notNull(), "cannot convert ${0} to ${1}", sqlTypeName, className(fieldType))
          .ok();
  }

  private static Map configure() {
    return TypeMap.nativeTypeMapBuilder()
          .autobox(true)
          .add(String.class, immutable(new StringReaders()))
          .add(Integer.class, immutable(new IntReaders()))
          .add(Double.class, immutable(new DoubleReaders()))
          .add(Long.class, immutable(new LongReaders()))
          .add(Float.class, immutable(new FloatReaders()))
          .add(Short.class, immutable(new ShortReaders()))
          .add(Byte.class, immutable(new ByteReaders()))
          .add(Boolean.class, immutable(new BooleanReaders()))
          .add(LocalDate.class, immutable(new LocalDateReaders()))
          .add(LocalDateTime.class, immutable(new LocalDateTimeReaders()))
          .add(Enum.class, immutable(new EnumReaders()))
          .freeze();
  }

  private static Map<Integer, ResultSetReader> immutable(ResultSetReaderLookup<?> src) {
    return Map.copyOf(src);
  }

}
