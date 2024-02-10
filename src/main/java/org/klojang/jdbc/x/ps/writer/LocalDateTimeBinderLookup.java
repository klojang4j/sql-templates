package org.klojang.jdbc.x.ps.writer;

import org.klojang.jdbc.x.ps.ValueBinder;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static java.sql.Types.*;
import static java.time.ZoneId.systemDefault;
import static org.klojang.jdbc.x.ps.ValueBinder.ANY_TO_STRING;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;
import static org.klojang.util.ObjectMethods.ifNotNull;

public final class LocalDateTimeBinderLookup extends ColumnWriterLookup<LocalDateTime> {

  @SuppressWarnings("rawtypes")
  public static final ValueBinder DEFAULT = localDateTimeToTimestamp();

  @SuppressWarnings("unchecked")
  public LocalDateTimeBinderLookup() {
    put(TIMESTAMP, DEFAULT);
    put(DATE, localDateTimeToSqlDate());
    put(BIGINT, localDateTimeToLong());
    addMultiple(ANY_TO_STRING, VARCHAR, CHAR);
  }

  private static ValueBinder<LocalDateTime, Timestamp> localDateTimeToTimestamp() {
    return new ValueBinder<>(SET_TIMESTAMP, x -> ifNotNull(x, Timestamp::valueOf));
  }

  private static ValueBinder<LocalDateTime, Date> localDateTimeToSqlDate() {
    return new ValueBinder<>(SET_DATE, x -> new Date(getEpochSeconds(x)));
  }

  private static ValueBinder<LocalDateTime, Long> localDateTimeToLong() {
    return new ValueBinder<>(SET_LONG, x -> getEpochMilli(x));
  }


  private static Long getEpochMilli(LocalDateTime x) {
    return x == null
          ? null
          : x.toInstant((ZoneOffset) systemDefault()).toEpochMilli();
  }

  private static Long getEpochSeconds(LocalDateTime x) {
    return x == null
          ? null
          : x.toLocalDate().atStartOfDay(systemDefault()).toEpochSecond();
  }

}
