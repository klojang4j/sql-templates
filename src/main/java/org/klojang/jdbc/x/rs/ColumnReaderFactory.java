package org.klojang.jdbc.x.rs;

import org.klojang.check.Check;
import org.klojang.collections.TypeMap;
import org.klojang.jdbc.DatabaseException;
import org.klojang.jdbc.util.SQLTypeUtil;
import org.klojang.jdbc.x.Err;
import org.klojang.jdbc.x.Msg;
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
import static java.sql.Types.*;
import static org.klojang.check.CommonChecks.notNull;
import static org.klojang.jdbc.util.SQLTypeUtil.getTypeName;
import static org.klojang.jdbc.x.Err.CANNOT_CONVERT_SQL_TYPE_TO_JAVA_TYPE;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;
import static org.klojang.util.ClassMethods.className;
import static org.klojang.util.ClassMethods.simpleClassName;

/**
 * Finds a ColumnReader for a given Java type.
 */
@SuppressWarnings("rawtypes")
public class ColumnReaderFactory {

  private static final Logger LOG = LoggerFactory.getLogger(ColumnReaderFactory.class);

  private static final ColumnReaderFactory INSTANCE = new ColumnReaderFactory();

  public static ColumnReaderFactory getInstance() {
    return INSTANCE;
  }

  private record Key(Class targetType, int columnType) { }

  // Predefined ColumnReaders for common types
  private final Map<Class, ColumnReaderLookup<?>> predefined;
  // ColumnReaders that are created on demand
  private final Map<Key, ColumnReader> custom = new HashMap<>();

  @SuppressWarnings("unchecked")
  private ColumnReaderFactory() {
    predefined = getPredefinedColumnReaders();
  }

  @SuppressWarnings({"unchecked"})
  public <T, U> ColumnReader<T, U> getReader(Class<U> targetType, int columnType) {
    ColumnReaderLookup<?> lookup = predefined.get(targetType);
    ColumnReader reader;
    if (lookup == null) {
      reader = createCustomReader(targetType, columnType);
      checkReaderNotNull(targetType, columnType, reader);
    } else {
      Check.that(columnType).is(SQLTypeUtil::isValidValue, Err.NO_SUCH_SQL_TYPE);
      reader = lookup.getColumnReader(columnType);
      if (reader == null) {
        reader = createCustomReader(targetType, columnType);
        checkReaderNotNull(targetType, columnType, reader);
      }
    }
    return reader;
  }

  private static Map getPredefinedColumnReaders() {
    return TypeMap.nativeTypeMapBuilder()
          .autobox(true)
          .add(String.class, new StringReaderLookup())
          .add(Integer.class, new IntReaderLookup())
          .add(Boolean.class, new BooleanReaderLookup())
          .add(Double.class, new DoubleReaderLookup())
          .add(Long.class, new LongReaderLookup())
          .add(Enum.class, new EnumReaderLookup())
          .add(BigDecimal.class, new BigDecimalReaderLookup())
          .add(LocalDate.class, new LocalDateReaderLookup())
          .add(LocalDateTime.class, new LocalDateTimeReaderLookup())
          .add(Short.class, new ShortReaderLookup())
          .add(Byte.class, new ByteReaderLookup())
          .add(Float.class, new FloatReaderLookup())
          .add(UUID.class, new UUIDReaderLookup())
          .freeze();
  }

  @SuppressWarnings("unchecked")
  private ColumnReader createCustomReader(Class targetType, int columnType) {
    Key key = new Key(targetType, columnType);
    ColumnReader reader = custom.get(key);
    if (reader != null) {
      return reader;
    }
    if (LOG.isTraceEnabled()) {
      LOG.trace(Msg.NO_PREDEFINED_COLUMN_READER,
            className(targetType),
            getTypeName(columnType));
    }
    if (columnType == VARCHAR || columnType == CHAR) {
      MethodHandle mh = findFactoryMethod(targetType, String.class);
      if (mh == null) {
        mh = findConstructor(targetType, String.class);
      }
      if (mh != null) {
        reader = new ColumnReader(GET_STRING, createAdapter(mh));
      }
    } else if (columnType == INTEGER) {
      MethodHandle mh = findFactoryMethod(targetType, int.class);
      if (mh == null) {
        mh = findFactoryMethod(targetType, Integer.class);
      }
      if (mh != null) {
        reader = new ColumnReader(GET_STRING, createAdapter(mh));
      }
    } else if (columnType == BIGINT) {
      MethodHandle mh = findFactoryMethod(targetType, long.class);
      if (mh == null) {
        mh = findFactoryMethod(targetType, Long.class);
      }
      if (mh != null) {
        reader = new ColumnReader(GET_LONG, createAdapter(mh));
      }
    } else if (columnType == NUMERIC || columnType == DECIMAL) {
      MethodHandle mh = findFactoryMethod(targetType, double.class);
      if (mh == null) {
        mh = findFactoryMethod(targetType, Double.class);
      }
      if (mh != null) {
        reader = new ColumnReader(GET_DOUBLE, createAdapter(mh));
      }
    } else if (columnType == VARBINARY || columnType == BINARY || columnType == LONGVARBINARY) {
      MethodHandle mh = findFactoryMethod(targetType, byte[].class);
      if (mh == null) {
        mh = findConstructor(targetType, byte[].class);
      }
      if (mh != null) {
        reader = new ColumnReader(GET_BYTES, createAdapter(mh));
      }
    }
    if (reader != null) {
      custom.put(key, reader);
    }
    return reader;
  }

  private MethodHandle findFactoryMethod(Class forType, Class fromType) {
    Method[] candidates = Arrays.stream(forType.getDeclaredMethods())
          .filter(method -> isFactoryMethod(method, fromType))
          .toArray(Method[]::new);
    if (candidates.length == 0) {
      LOG.trace("Unable to find a factory method that returns a {} from a {}",
            simpleClassName(forType),
            simpleClassName(fromType));
      return null;
    } else if (candidates.length > 1) {
      LOG.trace("Too many factory methods ({}) that return a {} from a {}",
            candidates.length,
            simpleClassName(forType),
            simpleClassName(fromType));
      return null;
    }
    Method method = candidates[0];
    LOG.trace("Will use {}.{}({})",
          simpleClassName(forType),
          method.getName(),
          simpleClassName(fromType));
    try {
      return MethodHandles.publicLookup().unreflect(method);
    } catch (IllegalAccessException e) {
      throw Utils.wrap(e);
    }
  }

  private static MethodHandle findConstructor(Class forType, Class fromType) {
    MethodHandles.Lookup lookup = MethodHandles.publicLookup();
    try {
      MethodHandle mh = lookup.findConstructor(forType, methodType(void.class, fromType));
      LOG.trace("Will use constructor new {}({})",
            simpleClassName(forType),
            simpleClassName(fromType));
      return mh;
    } catch (Exception e) {
      // ...
    }
    return null;
  }


  private boolean isFactoryMethod(Method m, Class fromType) {
    return Modifier.isPublic(m.getModifiers())
          && Modifier.isStatic(m.getModifiers())
          && m.getReturnType() == m.getDeclaringClass()
          && m.getParameterTypes().length == 1
          && m.getParameterTypes()[0] == fromType;
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

  private static <U> void checkReaderNotNull(Class<U> targetType,
        int columnType,
        ColumnReader reader) {
    Utils.check(reader).is(notNull(),
          CANNOT_CONVERT_SQL_TYPE_TO_JAVA_TYPE,
          getTypeName(columnType),
          className(targetType));
  }


}
