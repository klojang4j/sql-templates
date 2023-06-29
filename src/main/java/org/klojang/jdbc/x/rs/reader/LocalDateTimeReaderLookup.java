package org.klojang.jdbc.x.rs.reader;

import org.klojang.jdbc.x.rs.ColumnReader;
import org.klojang.jdbc.x.rs.ColumnReaderLookup;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.function.Function;

import static java.sql.Types.DATE;
import static java.sql.Types.TIMESTAMP;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;


public final class LocalDateTimeReaderLookup extends ColumnReaderLookup<LocalDateTime> {

  public LocalDateTimeReaderLookup() {
    add(DATE, new ColumnReader<>(GET_DATE, sqlDateToLocalDateTime()));
    add(TIMESTAMP, new ColumnReader<>(GET_TIMESTAMP, sqlTimestampToLocalDateTime()));
  }

  private static Function<Date, LocalDateTime> sqlDateToLocalDateTime() {
    return d -> d == null ? null : d.toLocalDate().atStartOfDay();
  }

  private static Function<Timestamp, LocalDateTime> sqlTimestampToLocalDateTime() {
    return d -> d == null ? null : d.toLocalDateTime();
  }
}
