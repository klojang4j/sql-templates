package org.klojang.jdbc.x.ps.writer;

import org.klojang.jdbc.x.ps.ValueBinder;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import java.sql.Date;
import java.time.LocalDate;
import java.util.function.Function;

import static java.sql.Types.*;
import static java.time.ZoneId.systemDefault;
import static org.klojang.jdbc.x.ps.ValueBinder.ANY_TO_STRING;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.*;
import static org.klojang.util.ObjectMethods.ifNotNull;

public final class LocalDateBinderLookup extends ColumnWriterLookup<LocalDate> {

  @SuppressWarnings("rawtypes")
  public static final ValueBinder DEFAULT = localDateToSqlDate();

  @SuppressWarnings("unchecked")
  public LocalDateBinderLookup() {
    put(DATE, DEFAULT);
    put(TIMESTAMP, localDateToTimeStamp());
    put(BIGINT, localDateToLong());
    addMultiple(ANY_TO_STRING, VARCHAR, CHAR);
  }

  private static ValueBinder<LocalDate, Date> localDateToSqlDate() {
    return new ValueBinder<>(SET_DATE, x -> ifNotNull(x, Date::valueOf));
  }

  private static ValueBinder<LocalDate, Object> localDateToTimeStamp() {
    return new ValueBinder<>(setObject(TIMESTAMP));
  }

  private static ValueBinder<LocalDate, Long> localDateToLong() {
    return new ValueBinder<>(SET_LONG, getEpochSeconds());
  }

  private static Function<LocalDate, Long> getEpochSeconds() {
    return x -> x == null ? null : getEpochSeconds(x);
  }

  private static long getEpochSeconds(LocalDate x) {
    return x.atStartOfDay(systemDefault()).toEpochSecond();
  }


}
