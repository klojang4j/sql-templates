package org.klojang.jdbc.x.rs;

import org.klojang.check.Check;
import org.klojang.collections.TypeMap;
import org.klojang.jdbc.KlojangSQLException;
import org.klojang.jdbc.x.rs.reader.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.lang.invoke.MethodType.methodType;
import static java.util.Arrays.asList;
import static org.klojang.check.CommonChecks.notNull;
import static org.klojang.jdbc.x.SQLTypeNames.getTypeName;
import static org.klojang.jdbc.x.rs.ResultSetMethod.GET_STRING;
import static org.klojang.util.ClassMethods.className;

/**
 * Finds a ColumnReader for a given Java type.
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

  private final Map<Class<?>, Map<Integer, ColumnReader>> predefined;
  private final Map<Class<?>, ColumnReader> adhoc = new HashMap<>();

  @SuppressWarnings("unchecked")
  private ColumnReaderFinder() {
    predefined = configure();
  }

  @SuppressWarnings({"unchecked"})
  public <T, U> ColumnReader<T, U> findReader(Class<U> fieldType, int sqlType) {
    Map<Integer, ColumnReader> readers = predefined.get(fieldType);
    ColumnReader reader;
    if (readers == null) {
      // We will call ResultSet.getString() and pass the string to a reasonably named
      // factory method on fieldType; one that takes a string and returns an instance of
      // fieldType.
      reader = createUsingFactoryMethod(fieldType);
      Check.that(reader).is(notNull(), "type not supported: ${0}", className(fieldType));
    } else {
      // Implicitly checks that the specified int is one of the
      // static final int constants in java.sql.Types
      String sqlTypeName = getTypeName(sqlType);
      reader = readers.get(sqlType);
      Check.that(reader).is(notNull(),
            "cannot convert ${0} to ${1}",
            sqlTypeName,
            className(fieldType));
    }
    return reader;
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
          .add(UUID.class, immutable(new UUIDReaderLookup()))
          .freeze();
  }

  private static Map<Integer, ColumnReader> immutable(ColumnReaderLookup<?> src) {
    return Map.copyOf(src);
  }

  @SuppressWarnings("unchecked")
  private ColumnReader createUsingFactoryMethod(Class cls) {
    ColumnReader reader = adhoc.get(cls);
    if (reader == null) {
      MethodHandles.Lookup lookup = MethodHandles.publicLookup();
      MethodHandle mh = null;
      for (String method : asList("fromString", "forString", "parse", "read")) {
        try {
          mh = lookup.findStatic(cls, method, methodType(cls, String.class));
          break;
        } catch (NoSuchMethodException e) {
          //
        } catch (IllegalAccessException e) {
          throw new KlojangSQLException(e);
        }
      }
      if (mh != null) {
        MethodHandle found = mh;
        Adapter adapter = (x, y) -> {
          try {
            return found.invoke(x);
          } catch (Throwable t) {
            throw new KlojangSQLException(t);
          }
        };
        reader = new ColumnReader<>(GET_STRING, adapter);
        adhoc.put(cls, reader);
      }
    }
    return reader;
  }

}
