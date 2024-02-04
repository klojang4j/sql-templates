package org.klojang.jdbc.x.rs;

import org.klojang.collections.TypeMap;
import org.klojang.jdbc.DatabaseException;
import org.klojang.jdbc.x.Utils;
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
public class ColumnReaderFactory {

  private static final Logger LOG = LoggerFactory.getLogger(ColumnReaderFactory.class);
  private static final String TYPE_NOT_SUPPORTED = "type not supported: ${0}";
  private static final String NOT_CONVERTIBLE = "cannot convert ${0} to ${1}";

  private static ColumnReaderFactory INSTANCE;

  public static ColumnReaderFactory getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ColumnReaderFactory();
    }
    return INSTANCE;
  }

  // Predefined ColumnReaders for common types
  private final Map<Class<?>, Map<Integer, ColumnReader>> predefined;
  // ColumnReaders that are created on demand
  private final Map<Class<?>, ColumnReader> onDemand = new HashMap<>();

  @SuppressWarnings("unchecked")
  private ColumnReaderFactory() {
    predefined = configure();
  }

  @SuppressWarnings({"unchecked"})
  public <T, U> ColumnReader<T, U> getReader(Class<U> fieldType, int sqlType) {
    Map<Integer, ColumnReader> readers = predefined.get(fieldType);
    ColumnReader reader;
    if (readers == null) {
      // Then we'll call ResultSet.getString() and pass the string to a static factory
      // method or constructor on the provided class, one that takes a single string and
      // returns an instance of fieldType. Provided such a method or constructor exists
      // of course. If not, we give up and throw an exception.
      reader = createOnDemand(fieldType);
      Utils.check(reader).is(notNull(), TYPE_NOT_SUPPORTED, className(fieldType));
    } else {
      // Implicitly checks that the specified int is one of the
      // static final int constants in java.sql.Types
      String typeName = getTypeName(sqlType);
      reader = readers.get(sqlType);
      Utils.check(reader).is(notNull(), NOT_CONVERTIBLE, typeName, className(fieldType));
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
  private ColumnReader createOnDemand(Class cls) {
    ColumnReader reader = onDemand.get(cls);
    if (reader != null) {
      return reader;
    }
    LOG.trace("No predefined ColumnReader available for {}. Will try to construct one",
          className(cls));
    MethodHandle mh = ifNull(findFactoryMethod(cls), () -> findConstructor(cls));
    if (mh != null) {
      onDemand.put(cls, reader = new ColumnReader<>(GET_STRING, createAdapter(mh)));
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
        LOG.trace("Will use static factory method {}.{}(String arg0)",
              simpleClassName(cls),
              method);
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
        throw new DatabaseException(t);
      }
    };
  }
}
