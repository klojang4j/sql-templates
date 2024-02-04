package org.klojang.jdbc.x.rs;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;

import org.klojang.jdbc.DatabaseException;
import org.klojang.util.ExceptionMethods;

import static java.lang.invoke.MethodHandles.lookup;

/*
 * Represents one of the get methods of ResultSet, like ResultSet.getString(columnIndex)
 */
public final class ResultSetMethod<COLUMN_TYPE> {

  public static final ResultSetMethod<String> GET_STRING
        = getter("getString", String.class);
  public static final ResultSetMethod<Integer> GET_INT
        = getter("getInt", int.class);
  public static final ResultSetMethod<Float> GET_FLOAT
        = getter("getFloat", float.class);
  public static final ResultSetMethod<Double> GET_DOUBLE
        = getter("getDouble", double.class);
  public static final ResultSetMethod<Long> GET_LONG
        = getter("getLong", long.class);
  public static final ResultSetMethod<Short> GET_SHORT
        = getter("getShort", short.class);
  public static final ResultSetMethod<Byte> GET_BYTE
        = getter("getByte", byte.class);
  public static final ResultSetMethod<Boolean> GET_BOOLEAN
        = getter("getBoolean", boolean.class);
  public static final ResultSetMethod<Date> GET_DATE
        = getter("getDate", Date.class);
  public static final ResultSetMethod<Time> GET_TIME
        = getter("getTime", Time.class);
  public static final ResultSetMethod<Timestamp> GET_TIMESTAMP
        = getter("getTimestamp", Timestamp.class);
  public static final ResultSetMethod<BigDecimal> GET_BIG_DECIMAL
        = getter("getBigDecimal", BigDecimal.class);

  // Invokes <T> ResultSet.getObject(columnIndex, Class<T>)
  public static <T> ResultSetMethod<T> objectGetter(Class<T> returnType) {
    MethodType mt = MethodType.methodType(Object.class, int.class, Class.class);
    MethodHandle mh;
    try {
      mh = lookup().findVirtual(ResultSet.class, "getObject", mt);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw ExceptionMethods.uncheck(e);
    }
    return new ResultSetMethod<>(mh, returnType);
  }

  private final MethodHandle method;

  // If this is ResultSetMethod invokes ResultSet.getObject(int, Class), then
  // classArg will be the Class object passed in as the second argument to the
  // getObject() method. In any other case classArg will be null.
  private final Class<?> classArg;

  private ResultSetMethod(MethodHandle method) {
    this(method, null);
  }

  private ResultSetMethod(MethodHandle method, Class<?> classArg) {
    this.method = method;
    this.classArg = classArg;
  }

  public COLUMN_TYPE invoke(ResultSet rs, int columnIndex) throws Throwable {
    COLUMN_TYPE val;
    if (classArg == null) {
      val = (COLUMN_TYPE) method.invoke(rs, columnIndex);
    } else {
      val = (COLUMN_TYPE) method.invoke(rs, columnIndex, classArg);
    }
    return rs.wasNull() ? null : val;
  }

  private static <T> ResultSetMethod<T> getter(String methodName, Class<T> returnType) {
    MethodType mt = MethodType.methodType(returnType, int.class);
    MethodHandle mh;
    try {
      mh = lookup().findVirtual(ResultSet.class, methodName, mt);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new DatabaseException(e);
    }
    return new ResultSetMethod<>(mh);
  }
}
