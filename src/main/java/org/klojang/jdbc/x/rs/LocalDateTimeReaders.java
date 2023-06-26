package org.klojang.jdbc.x.rs;

import java.time.LocalDateTime;

import static java.sql.Types.DATE;
import static java.sql.Types.TIMESTAMP;

final class LocalDateTimeReaders extends ReaderLookup<LocalDateTime> {

  LocalDateTimeReaders() {
    add(DATE, new ResultSetReader<>(
          ResultSetMethod.GET_DATE,
          d -> d == null ? null : d.toLocalDate().atStartOfDay()));
    add(TIMESTAMP, new ResultSetReader<>(
          ResultSetMethod.GET_TIMESTAMP,
          d -> d == null ? null : d.toLocalDateTime()));
  }
}
