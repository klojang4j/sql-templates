package org.klojang.jdbc.x.ps;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.sql.Timestamp;

import org.klojang.util.ExceptionMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.invoke.MethodHandles.lookup;
import static org.klojang.util.StringMethods.lpad;

/**
 * Represents one of the setXXX() methods of PreparedStatement
 */
public final class PreparedStatementMethod<PARAM_TYPE> {

  private static final Logger LOG = LoggerFactory.getLogger(PreparedStatementMethod.class);

  public static final PreparedStatementMethod<String> SET_STRING
        = setter("setString", String.class);
  public static final PreparedStatementMethod<Integer> SET_INT
        = setter("setInt", int.class);
  public static final PreparedStatementMethod<Double> SET_DOUBLE
        = setter("setDouble", double.class);
  public static final PreparedStatementMethod<Long> SET_LONG
        = setter("setLong", long.class);
  public static final PreparedStatementMethod<Float> SET_FLOAT
        = setter("setFloat", float.class);
  public static final PreparedStatementMethod<Short> SET_SHORT
        = setter("setShort", short.class);
  public static final PreparedStatementMethod<Byte> SET_BYTE
        = setter("setByte", byte.class);
  public static final PreparedStatementMethod<Boolean> SET_BOOLEAN
        = setter("setBoolean", boolean.class);
  public static final PreparedStatementMethod<BigDecimal> SET_BIG_DECIMAL
        = setter("setBigDecimal", BigDecimal.class);
  public static final PreparedStatementMethod<Date> SET_DATE
        = setter("setDate", Date.class);
  public static final PreparedStatementMethod<Time> SET_TIME
        = setter("setTime", Time.class);
  public static final PreparedStatementMethod<Timestamp> SET_TIMESTAMP
        = setter("setTimestamp", Timestamp.class);

  private final String name;
  private final MethodHandle method;
  private final Class<PARAM_TYPE> paramType;
  private final Integer sqlType;

  private PreparedStatementMethod(String name, MethodHandle method, Class<PARAM_TYPE> paramType) {
    this(name, method, paramType, null);
  }

  private PreparedStatementMethod(
        String name,
        MethodHandle method,
        Class<PARAM_TYPE> paramType,
        Integer sqlType) {
    this.name = name;
    this.method = method;
    this.paramType = paramType;
    this.sqlType = sqlType;
  }

  Class<PARAM_TYPE> getParamType() {
    return paramType;
  }

  public static PreparedStatementMethod<Object> setObject(int targetSqlType) {
    MethodType mt = MethodType.methodType(void.class, int.class, Object.class, int.class);
    MethodHandle mh;
    try {
      mh = lookup().findVirtual(PreparedStatement.class, "setObject", mt);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw ExceptionMethods.uncheck(e);
    }
    return new PreparedStatementMethod<>("setObject", mh, Object.class, targetSqlType);
  }

  void bindValue(PreparedStatement ps, int paramIndex, PARAM_TYPE paramValue)
        throws Throwable {
    LOG.trace("-----> Parameter {}: {}", paramIndex, paramValue);
    if (paramValue == null) {
      SET_STRING.method.invoke(ps, paramIndex, (String) null);
    } else if (sqlType == null) {
      method.invoke(ps, paramIndex, paramValue);
    } else {
      method.invoke(ps, paramIndex, paramValue, sqlType);
    }
  }

  private static <X> PreparedStatementMethod<X> setter(String methodName, Class<X> paramType) {
    MethodType mt = MethodType.methodType(void.class, int.class, paramType);
    MethodHandle mh;
    try {
      mh = lookup().findVirtual(PreparedStatement.class, methodName, mt);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw ExceptionMethods.uncheck(e);
    }
    return new PreparedStatementMethod<>(methodName, mh, paramType);
  }
}
