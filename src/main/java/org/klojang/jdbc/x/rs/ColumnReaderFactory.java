package org.klojang.jdbc.x.rs;

import org.klojang.collections.TypeMap;
import org.klojang.jdbc.DatabaseException;
import org.klojang.jdbc.x.Utils;
import org.klojang.jdbc.x.rs.reader.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.lang.invoke.MethodType.methodType;
import static org.klojang.check.CommonChecks.notNull;
import static org.klojang.jdbc.util.SQLTypeUtil.getTypeName;
import static org.klojang.jdbc.x.rs.ResultSetMethod.GET_STRING;
import static org.klojang.util.ClassMethods.className;
import static org.klojang.util.ClassMethods.simpleClassName;

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
  private final Map<Class<?>, ColumnReaderLookup<?>> predefined;
  // ColumnReaders that are created on demand
  private final Map<Class<?>, ColumnReader> onDemand = new HashMap<>();

  @SuppressWarnings("unchecked")
  private ColumnReaderFactory() {
    predefined = configure();
  }

  @SuppressWarnings({"unchecked"})
  public <T, U> ColumnReader<T, U> getReader(Class<U> targetType, int columnType) {
    ColumnReaderLookup<?> lookup = predefined.get(targetType);
    ColumnReader reader;
    if (lookup == null) {
      // Then we'll call ResultSet.getString() and pass the string to a static factory
      // method or constructor on the provided class, one that takes a single string and
      // returns an instance of fieldType. Provided such a method or constructor exists
      // of course. If not, we give up and throw an exception.
      reader = createOnDemand(targetType);
      Utils.check(reader).is(notNull(), TYPE_NOT_SUPPORTED, className(targetType));
    } else {
      // Implicitly checks that the specified int is one of the static final int
      // constants in java.sql.Types
      String typeName = getTypeName(columnType);
      reader = lookup.getColumnReader(columnType);
      Utils.check(reader).is(notNull(), NOT_CONVERTIBLE, typeName, className(targetType));
    }
    return reader;
  }

  private static Map configure() {
    return TypeMap.nativeTypeMapBuilder()
          .autobox(true)
          .add(BigDecimal.class, new BigDecimalReaderLookup())
          .add(Boolean.class, new BooleanReaderLookup())
          .add(Byte.class, new ByteReaderLookup())
          .add(Double.class, new DoubleReaderLookup())
          .add(Enum.class, new EnumReaderLookup())
          .add(Float.class, new FloatReaderLookup())
          .add(Integer.class, new IntReaderLookup())
          .add(LocalDate.class, new LocalDateReaderLookup())
          .add(LocalDateTime.class, new LocalDateTimeReaderLookup())
          .add(Long.class, new LongReaderLookup())
          .add(Short.class, new ShortReaderLookup())
          .add(String.class, new StringReaderLookup())
          .add(UUID.class, new UUIDReaderLookup())
          .freeze();
  }

  @SuppressWarnings("unchecked")
  private ColumnReader createOnDemand(Class cls) {
    ColumnReader reader = onDemand.get(cls);
    if (reader != null) {
      return reader;
    }
    LOG.trace("No predefined ColumnReader {}. Searching for factory method on {}",
          className(cls),
          className(cls));
    MethodHandle mh = findFactoryMethod(cls);
    if (mh == null) {
      mh = findConstructor(cls);
    }
    if (mh != null) {
      onDemand.put(cls, reader = new ColumnReader<>(GET_STRING, createAdapter(mh)));
      return reader;
    }
    return null;
  }

  private MethodHandle findFactoryMethod(Class cls) {
    Method[] candidates = Arrays.stream(cls.getDeclaredMethods())
          .filter(this::isFactoryMethod)
          .toArray(Method[]::new);
    if (candidates.length == 0) {
      LOG.trace("No factory method found. Searching for constructor");
      return null;
    } else if (candidates.length > 1) {
      LOG.trace("Too many factory methods. Searching for constructor");
      return null;
    }
    try {
      Method m = candidates[0];
      LOG.trace("Will use {}.{}(String)", simpleClassName(cls), m.getName());
      return MethodHandles.publicLookup().unreflect(candidates[0]);
    } catch (IllegalAccessException e) {
      throw Utils.wrap(e);
    }
  }

  private static MethodHandle findConstructor(Class cls) {
    MethodHandles.Lookup lookup = MethodHandles.publicLookup();
    try {
      MethodHandle mh = lookup.findConstructor(cls, methodType(void.class, String.class));
      LOG.trace("Will use constructor {}(String)", simpleClassName(cls));
      return mh;
    } catch (Exception e) { }
    return null;
  }


  private boolean isFactoryMethod(Method m) {
    return Modifier.isPublic(m.getModifiers())
          && Modifier.isStatic(m.getModifiers())
          && m.getReturnType() == m.getDeclaringClass()
          && m.getParameterTypes().length == 1
          && m.getParameterTypes()[0] == String.class;
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
