package org.klojang.jdbc.x.rs.reader;

import org.klojang.jdbc.x.rs.ResultSetReader;
import org.klojang.jdbc.x.rs.ResultSetReaderLookup;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.function.Function;

import static java.sql.Types.DATE;
import static java.sql.Types.TIMESTAMP;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;


public final class LocalDateTimeReaders extends ResultSetReaderLookup<LocalDateTime> {

  public LocalDateTimeReaders() {
    add(DATE, new ResultSetReader<>(GET_DATE, sqlDateToLocalDateTime()));
    add(TIMESTAMP, new ResultSetReader<>(GET_TIMESTAMP, sqlTimestampToLocalDateTime()));
  }

  private static Function<Date, LocalDateTime> sqlDateToLocalDateTime() {
    return d -> d == null ? null : d.toLocalDate().atStartOfDay();
  }

  private static Function<Timestamp, LocalDateTime> sqlTimestampToLocalDateTime() {
    return d -> d == null ? null : d.toLocalDateTime();
  }
}
