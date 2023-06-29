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

  public static final ColumnWriter<LocalDate, Date> DEFAULT =
        new ColumnWriter<>(SET_DATE, d -> ifNotNull(d, Date::valueOf));

  public LocalDateWriterLookup() {
    put(DATE, DEFAULT);
    put(TIMESTAMP, new ColumnWriter<LocalDate, Object>(setObject(TIMESTAMP)));
  }


}
