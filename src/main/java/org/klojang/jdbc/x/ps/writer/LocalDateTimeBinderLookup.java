package org.klojang.jdbc.x.ps.writer;

import org.klojang.jdbc.x.ps.ValueBinder;
import org.klojang.jdbc.x.ps.ValueBinderLookup;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.TimeZone;

import static java.sql.Types.*;
import static java.time.ZoneId.systemDefault;
import static org.klojang.jdbc.x.ps.ValueBinder.ANY_TO_STRING;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;
import static org.klojang.util.ObjectMethods.ifNotNull;

public final class LocalDateTimeBinderLookup extends ValueBinderLookup<LocalDateTime> {

  @SuppressWarnings("rawtypes")
  public static final ValueBinder DEFAULT = localDateTimeToTimestamp();

  @SuppressWarnings("unchecked")
  public LocalDateTimeBinderLookup() {
    put(TIMESTAMP, DEFAULT);
    put(DATE, localDateTimeToSqlDate());
    // Friendly reminder on how to read this:
    // * if we are dealing with a LocalDateTime value
    // * and the database type is Types.BIGINT
    // * then we are going to call PreparedStatement.setLong() and we will extract
    //   extract the milliseconds-since-1970 from the LocalDateTime
    put(BIGINT, localDateTimeToLong());
    addMultiple(ANY_TO_STRING, VARCHAR, CHAR);
  }

  private static ValueBinder<LocalDateTime, Timestamp> localDateTimeToTimestamp() {
    return new ValueBinder<>(SET_TIMESTAMP, x -> ifNotNull(x, Timestamp::valueOf));
  }

  private static ValueBinder<LocalDateTime, Date> localDateTimeToSqlDate() {
    return new ValueBinder<>(SET_DATE, x -> new Date(getEpochMilliGMT(x)));
  }

  private static ValueBinder<LocalDateTime, Long> localDateTimeToLong() {
    return new ValueBinder<>(SET_LONG, x -> getEpochMilli(x));
  }

  private static Long getEpochMilli(LocalDateTime x) {
    return x.toInstant((ZoneOffset) systemDefault()).toEpochMilli();
  }

  private static Long getEpochMilliGMT(LocalDateTime x) {
    return x.toInstant(ZoneOffset.UTC).toEpochMilli();
  }

}
