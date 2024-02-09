package org.klojang.jdbc.x.ps.writer;

import org.klojang.jdbc.x.ps.ColumnWriter;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import java.sql.Date;
import java.time.LocalDate;

import static java.sql.Types.DATE;
import static java.sql.Types.TIMESTAMP;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.SET_DATE;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.setObject;
import static org.klojang.util.ObjectMethods.ifNotNull;

public final class LocalDateWriterLookup extends ColumnWriterLookup<LocalDate> {

  @SuppressWarnings("rawtypes")
  public static final ColumnWriter DEFAULT = localDateToSqlDate();

  public LocalDateWriterLookup() {
    put(DATE, DEFAULT);
    put(TIMESTAMP, localDateToTimeStamp());
  }

  private static ColumnWriter<LocalDate, Date> localDateToSqlDate() {
    return new ColumnWriter<>(SET_DATE, d -> ifNotNull(d, Date::valueOf));
  }

  private static ColumnWriter<LocalDate, Object> localDateToTimeStamp() {
    return new ColumnWriter<>(setObject(TIMESTAMP));
  }


}
