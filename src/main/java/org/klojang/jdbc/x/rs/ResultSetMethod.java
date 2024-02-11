package org.klojang.jdbc.x.rs;

import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/*
 * Represents one of the get methods of ResultSet, like ResultSet.getString(columnIndex)
 */
public abstract sealed class ResultSetMethod<COLUMN_TYPE> {

  //@formatter:off
  private static final class GetString extends ResultSetMethod<String> {
    String invoke(ResultSet rs, int idx)throws SQLException {  return rs.getString(idx); }
  }
  private static final class GetInt extends ResultSetMethod<Integer> {
    Integer invoke(ResultSet rs, int idx)throws SQLException {  return rs.getInt(idx); }
  }
  private static final class GetFloat extends ResultSetMethod<Float> {
    Float invoke(ResultSet rs, int idx)throws SQLException {  return rs.getFloat(idx); }
  }
  private static final class GetDouble extends ResultSetMethod<Double> {
    Double invoke(ResultSet rs, int idx)throws SQLException {  return rs.getDouble(idx); }
  }
  private static final class GetLong extends ResultSetMethod<Long> {
    Long invoke(ResultSet rs, int idx)throws SQLException {  return rs.getLong(idx); }
  }
  private static final class GetShort extends ResultSetMethod<Short> {
    Short invoke(ResultSet rs, int idx)throws SQLException {  return rs.getShort(idx); }
  }
  private static final class GetByte extends ResultSetMethod<Byte> {
    Byte invoke(ResultSet rs, int idx)throws SQLException {  return rs.getByte(idx); }
  }
  private static final class GetBoolean extends ResultSetMethod<Boolean> {
    Boolean invoke(ResultSet rs, int idx)throws SQLException {  return rs.getBoolean(idx); }
  }
  private static final class GetDate extends ResultSetMethod<Date> {
    Date invoke(ResultSet rs, int idx)throws SQLException {  return rs.getDate(idx); }
  }
  private static final class GetTime extends ResultSetMethod<Time> {
    Time invoke(ResultSet rs, int idx)throws SQLException {  return rs.getTime(idx); }
  }
  private static final class GetTimestamp extends ResultSetMethod<Timestamp> {
    Timestamp invoke(ResultSet rs, int idx)throws SQLException {  return rs.getTimestamp(idx); }
  }
  private static final class GetBD extends ResultSetMethod<BigDecimal> {
    BigDecimal invoke(ResultSet rs, int idx)throws SQLException {  return rs.getBigDecimal(idx); }
  }
  private static final class GetBytes extends ResultSetMethod<byte[]> {
    byte[] invoke(ResultSet rs, int idx)throws SQLException {  return rs.getBytes(idx); }
  }
  //@formatter:on

  private static final class GetObject<T> extends ResultSetMethod<T> {

    private final Class<T> returnType;

    GetObject(Class<T> returnType) {
      this.returnType = returnType;
    }

    @Override
    T invoke(ResultSet rs, int columnIndex) throws SQLException {
      return rs.getObject(columnIndex, returnType);
    }
  }

  public static final ResultSetMethod<String> GET_STRING = new GetString();
  public static final ResultSetMethod<Integer> GET_INT = new GetInt();
  public static final ResultSetMethod<Float> GET_FLOAT = new GetFloat();
  public static final ResultSetMethod<Double> GET_DOUBLE = new GetDouble();
  public static final ResultSetMethod<Long> GET_LONG = new GetLong();
  public static final ResultSetMethod<Short> GET_SHORT = new GetShort();
  public static final ResultSetMethod<Byte> GET_BYTE = new GetByte();
  public static final ResultSetMethod<Boolean> GET_BOOLEAN = new GetBoolean();
  public static final ResultSetMethod<Date> GET_DATE = new GetDate();
  public static final ResultSetMethod<Time> GET_TIME = new GetTime();
  public static final ResultSetMethod<Timestamp> GET_TIMESTAMP = new GetTimestamp();
  public static final ResultSetMethod<BigDecimal> GET_BIG_DECIMAL = new GetBD();
  public static final ResultSetMethod<byte[]> GET_BYTES = new GetBytes();

  @SuppressWarnings("rawtypes")
  private static final Map<Class, ResultSetMethod> objectGetters = new HashMap<>();

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static <T> ResultSetMethod<T> objectGetter(Class<T> returnType) {
    return objectGetters.computeIfAbsent(returnType, GetObject::new);
  }

  abstract COLUMN_TYPE invoke(ResultSet rs, int columnIndex) throws SQLException;

}
