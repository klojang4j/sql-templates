package org.klojang.jdbc.x.ps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents one of the setXXX() methods of PreparedStatement.
 *
 * @param <PARAM_TYPE> the type of the value passed to
 *       PreparedStatement.setXXX(parameterIndex, value)
 */
public abstract class PreparedStatementMethod<PARAM_TYPE> {

  @SuppressWarnings({"unused"})
  private static final Logger LOG = LoggerFactory.getLogger(PreparedStatementMethod.class);

  //@formatter:off
  private static final class SetString extends PreparedStatementMethod<String> {
    SetString() { super(String.class); }
    void bindValue(PreparedStatement ps, int idx, String val) throws SQLException { ps.setString(idx, val); }
  }
  private static final class SetInt extends PreparedStatementMethod<Integer> {
    SetInt() { super(int.class); }
    void bindValue(PreparedStatement ps, int idx, Integer val) throws SQLException { ps.setInt(idx, val); }
  }
  private static final class SetDouble extends PreparedStatementMethod<Double> {
    SetDouble() { super(double.class); }
    void bindValue(PreparedStatement ps, int idx, Double val) throws SQLException { ps.setDouble(idx, val); }
  }
  private static final class SetLong extends PreparedStatementMethod<Long> {
    SetLong() { super(long.class); }
    void bindValue(PreparedStatement ps, int idx, Long val) throws SQLException { ps.setLong(idx, val); }
  }
  private static final class SetFloat extends PreparedStatementMethod<Float> {
    SetFloat() { super(float.class); }
    void bindValue(PreparedStatement ps, int idx, Float val) throws SQLException { ps.setFloat(idx, val); }
  }
  private static final class SetShort extends PreparedStatementMethod<Short> {
    SetShort() { super(short.class); }
    void bindValue(PreparedStatement ps, int idx, Short val) throws SQLException { ps.setShort(idx, val); }
  }
  private static final class SetByte extends PreparedStatementMethod<Byte> {
    SetByte() { super(byte.class); }
    void bindValue(PreparedStatement ps, int idx, Byte val) throws SQLException { ps.setByte(idx, val); }
  }
  private static final class SetBoolean extends PreparedStatementMethod<Boolean> {
    SetBoolean() { super(boolean.class); }
    void bindValue(PreparedStatement ps, int idx, Boolean val) throws SQLException { ps.setBoolean(idx, val); }
  }
  private static final class SetBD extends PreparedStatementMethod<BigDecimal> {
    SetBD() { super(BigDecimal.class); }
    void bindValue(PreparedStatement ps, int idx, BigDecimal val) throws SQLException { ps.setBigDecimal(idx, val); }
  }
  private static final class SetDate extends PreparedStatementMethod<Date> {
    SetDate() { super(Date.class); }
    void bindValue(PreparedStatement ps, int idx, Date val) throws SQLException { ps.setDate(idx, val); }
  }
  private static final class SetTime extends PreparedStatementMethod<Time> {
    SetTime() { super(Time.class); }
    void bindValue(PreparedStatement ps, int idx, Time val) throws SQLException { ps.setTime(idx, val); }
  }
  private static final class SetTimestamp extends PreparedStatementMethod<Timestamp> {
    SetTimestamp() { super(Timestamp.class); }
    void bindValue(PreparedStatement ps, int idx, Timestamp val) throws SQLException { ps.setTimestamp(idx, val); }
  }
  //@formatter:on

  public static final PreparedStatementMethod<String> SET_STRING = new SetString();
  public static final PreparedStatementMethod<Integer> SET_INT = new SetInt();
  public static final PreparedStatementMethod<Double> SET_DOUBLE = new SetDouble();
  public static final PreparedStatementMethod<Long> SET_LONG = new SetLong();
  public static final PreparedStatementMethod<Float> SET_FLOAT = new SetFloat();
  public static final PreparedStatementMethod<Short> SET_SHORT = new SetShort();
  public static final PreparedStatementMethod<Byte> SET_BYTE = new SetByte();
  public static final PreparedStatementMethod<Boolean> SET_BOOLEAN = new SetBoolean();
  public static final PreparedStatementMethod<BigDecimal> SET_BIG_DECIMAL = new SetBD();
  public static final PreparedStatementMethod<Date> SET_DATE = new SetDate();
  public static final PreparedStatementMethod<Time> SET_TIME = new SetTime();
  public static final PreparedStatementMethod<Timestamp> SET_TIMESTAMP = new SetTimestamp();

  private static final Map<Integer, PreparedStatementMethod<Object>> objectSetters = new HashMap<>();

  private final Class<PARAM_TYPE> paramType;

  private PreparedStatementMethod(Class<PARAM_TYPE> paramType) {
    this.paramType = paramType;
  }

  Class<PARAM_TYPE> getParamType() {
    return paramType;
  }

  public static PreparedStatementMethod<Object> setObject(int targetSqlType) {
    PreparedStatementMethod<Object> m = objectSetters.get(targetSqlType);
    if (m == null) {
      m = new PreparedStatementMethod<>(Object.class) {

        @Override
        void bindValue(PreparedStatement ps, int paramIndex, Object paramValue)
              throws SQLException {
          ps.setObject(paramIndex, paramValue, targetSqlType);
        }
      };
      objectSetters.put(targetSqlType, m);
    }
    return m;
  }

  abstract void bindValue(PreparedStatement ps, int paramIndex, PARAM_TYPE paramValue)
        throws SQLException;

}
