package org.klojang.jdbc.x.rs;

import org.klojang.check.Check;
import org.klojang.collections.TypeMap;
import org.klojang.jdbc.KlojangSQLException;
import org.klojang.jdbc.x.rs.reader.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.lang.invoke.MethodType.methodType;
import static org.klojang.check.CommonChecks.notNull;
import static org.klojang.jdbc.x.SQLTypeNames.getTypeName;
import static org.klojang.jdbc.x.rs.ResultSetMethod.GET_STRING;
import static org.klojang.util.ArrayMethods.pack;
import static org.klojang.util.ClassMethods.className;
import static org.klojang.util.ClassMethods.simpleClassName;
import static org.klojang.util.ObjectMethods.ifNull;

/**
 * Finds a ColumnReader for a given Java type.
 */
@SuppressWarnings("rawtypes")
public class ColumnReaderFinder {

  private static final Logger LOG = LoggerFactory.getLogger(ColumnReaderFinder.class);

  private static ColumnReaderFinder INSTANCE;

  public static ColumnReaderFinder getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ColumnReaderFinder();
    }
    return INSTANCE;
  }

  // Predefined ColumnReaders for common types
  private final Map<Class<?>, Map<Integer, ColumnReader>> predefined;
  // ColumnReaders that are created on demand
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
      // Then we'll call ResultSet.getString() and pass the string to a static factory
      // method on fieldType (or at least something we guess is a static factory method);
      // one that takes a string and returns an instance of fieldType.
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
    if (reader != null) {
      return reader;
    }
    LOG.trace("No standard ColumnReader available for {}. Will try to construct one",
          className(cls));
    MethodHandle mh = ifNull(findFactoryMethod(cls), () -> findConstructor(cls));
    if (mh != null) {
      adhoc.put(cls, reader = new ColumnReader<>(GET_STRING, createAdapter(mh)));
      return reader;
    }
    return null;
  }

  private static final String[] FACTORY_CANDIDATES = pack("fromString",
        "create",
        "valueOf",
        "from",
        "newInstance",
        "getInstance",
        "parse");

  private static MethodHandle findFactoryMethod(Class cls) {
    MethodHandles.Lookup lookup = MethodHandles.publicLookup();
    for (String method : FACTORY_CANDIDATES) {
      try {
        MethodHandle mh = lookup.findStatic(cls, method, methodType(cls, String.class));
        LOG.trace("Will use {}.{}(String arg0)", simpleClassName(cls), method);
        return mh;
      } catch (Exception e) { /* next one, then ... */ }
    }
    return null;
  }

  private static MethodHandle findConstructor(Class cls) {
    MethodHandles.Lookup lookup = MethodHandles.publicLookup();
    try {
      MethodHandle mh = lookup.findConstructor(cls, methodType(void.class, String.class));
      LOG.trace("Will use constructor {}(String arg0)", simpleClassName(cls));
      return mh;
    } catch (Exception e) { }
    return null;
  }

  private static Adapter createAdapter(MethodHandle mh) {
    return (x, y) -> {
      try {
        return mh.invoke(x);
      } catch (Throwable t) {
        throw new KlojangSQLException(t);
      }
    };
  }
}
