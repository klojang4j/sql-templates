package org.klojang.jdbc.x.ps.writer;

import org.klojang.jdbc.x.ps.ColumnWriter;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import java.sql.Date;
import java.time.LocalDate;
import java.util.function.Function;

import static java.sql.Types.*;
import static java.time.ZoneId.systemDefault;
import static org.klojang.jdbc.x.ps.ColumnWriter.ANY_TO_STRING;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;
import static org.klojang.util.ObjectMethods.ifNotNull;

public final class LocalDateWriterLookup extends ColumnWriterLookup<LocalDate> {

  @SuppressWarnings("rawtypes")
  public static final ColumnWriter DEFAULT = localDateToSqlDate();

  @SuppressWarnings("unchecked")
  public LocalDateWriterLookup() {
    put(DATE, DEFAULT);
    put(TIMESTAMP, localDateToTimeStamp());
    put(BIGINT, localDateToLong());
    addMultiple(ANY_TO_STRING, VARCHAR, CHAR);
  }

  private static ColumnWriter<LocalDate, Date> localDateToSqlDate() {
    return new ColumnWriter<>(SET_DATE, x -> ifNotNull(x, Date::valueOf));
  }

  private static ColumnWriter<LocalDate, Object> localDateToTimeStamp() {
    return new ColumnWriter<>(setObject(TIMESTAMP));
  }

  private static ColumnWriter<LocalDate, Long> localDateToLong() {
    return new ColumnWriter<>(SET_LONG, getEpochSeconds());
  }

  private static Function<LocalDate, Long> getEpochSeconds() {
    return x -> x == null ? null : getEpochSeconds(x);
  }

  private static long getEpochSeconds(LocalDate x) {
    return x.atStartOfDay(systemDefault()).toEpochSecond();
  }


}
