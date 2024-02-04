package org.klojang.jdbc.x.ps;

import org.klojang.jdbc.DatabaseException;
import org.klojang.util.ExceptionMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;

/**
 * Represents one of the setXXX() methods of PreparedStatement.
 *
 * @param <PARAM_TYPE> the type of the value passed to
 *       PreparedStatement.setXXX(parameterIndex, value)
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

  private static final Map<Integer, PreparedStatementMethod<Object>> setObjectMethods = new HashMap<>();

  private final String name;
  private final MethodHandle method;
  private final Class<PARAM_TYPE> paramType;
  private final Integer sqlType;

  private PreparedStatementMethod(String name,
        MethodHandle method,
        Class<PARAM_TYPE> paramType) {
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
    PreparedStatementMethod<Object> psm = setObjectMethods.get(targetSqlType);
    if (psm == null) {
      MethodType mt = methodType(void.class, int.class, Object.class, int.class);
      MethodHandle mh;
      try {
        mh = publicLookup().findVirtual(PreparedStatement.class, "setObject", mt);
      } catch (Exception e) {
        throw new DatabaseException(e);
      }
      psm = new PreparedStatementMethod<>("setObject", mh, Object.class, targetSqlType);
      setObjectMethods.put(targetSqlType, psm);
    }
    return psm;
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

  private static <X> PreparedStatementMethod<X> setter(String methodName,
        Class<X> paramType) {
    MethodType mt = methodType(void.class, int.class, paramType);
    MethodHandle mh;
    try {
      mh = lookup().findVirtual(PreparedStatement.class, methodName, mt);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw ExceptionMethods.uncheck(e);
    }
    return new PreparedStatementMethod<>(methodName, mh, paramType);
  }
}
