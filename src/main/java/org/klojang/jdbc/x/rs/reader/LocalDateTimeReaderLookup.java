package org.klojang.jdbc.x.rs.reader;

import org.klojang.jdbc.x.rs.ColumnReader;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.sql.Types.*;
import static java.time.ZoneId.systemDefault;
import static java.util.Map.Entry;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;


public final class LocalDateTimeReaderLookup extends AbstractColumnReaderLookup<LocalDateTime> {

  @Override
  List<Entry<Integer, ColumnReader<?, LocalDateTime>>> getColumnReaders() {
    List<Entry<Integer, ColumnReader<?, LocalDateTime>>> entries = new ArrayList<>(16);
    entries.add(entry(GET_DATE, sqlDateToLocalDateTime(), DATE));
    entries.add(entry(GET_TIMESTAMP, sqlTimestampToLocalDateTime(), TIMESTAMP));
    entries.add(entry(GET_LONG, longToLocalDateTime(), BIGINT));
    return entries;
  }

  private static Function<java.sql.Date, LocalDateTime> sqlDateToLocalDateTime() {
    return x -> x == null ? null : x.toLocalDate().atStartOfDay();
  }

  private static Function<Timestamp, LocalDateTime> sqlTimestampToLocalDateTime() {
    return d -> d == null ? null : d.toLocalDateTime();
  }

  private static Function<Long, LocalDateTime> longToLocalDateTime() {
    return x -> x == null
          ? null
          : Instant.ofEpochMilli(x).atZone(systemDefault()).toLocalDateTime();
  }

}
