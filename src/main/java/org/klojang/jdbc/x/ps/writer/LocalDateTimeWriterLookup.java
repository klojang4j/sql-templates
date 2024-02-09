package org.klojang.jdbc.x.ps.writer;

import org.klojang.jdbc.x.ps.ColumnWriter;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static java.sql.Types.*;
import static java.time.ZoneId.systemDefault;
import static org.klojang.jdbc.x.ps.ColumnWriter.ANY_TO_STRING;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;
import static org.klojang.util.ObjectMethods.ifNotNull;

public final class LocalDateTimeWriterLookup extends ColumnWriterLookup<LocalDateTime> {

  @SuppressWarnings("rawtypes")
  public static final ColumnWriter DEFAULT = localDateTimeToTimestamp();

  @SuppressWarnings("unchecked")
  public LocalDateTimeWriterLookup() {
    put(TIMESTAMP, DEFAULT);
    put(DATE, localDateTimeToSqlDate());
    put(BIGINT, localDateTimeToLong());
    addMultiple(ANY_TO_STRING, VARCHAR, CHAR);
  }

  private static ColumnWriter<LocalDateTime, Timestamp> localDateTimeToTimestamp() {
    return new ColumnWriter<>(SET_TIMESTAMP, x -> ifNotNull(x, Timestamp::valueOf));
  }

  private static ColumnWriter<LocalDateTime, java.sql.Date> localDateTimeToSqlDate() {
    return new ColumnWriter<>(SET_DATE, x -> new Date(getEpochSeconds(x)));
  }

  private static ColumnWriter<LocalDateTime, Long> localDateTimeToLong() {
    return new ColumnWriter<>(SET_LONG, x -> getEpochMilli(x));
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
