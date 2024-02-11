package org.klojang.jdbc.x.rs.reader;

import org.klojang.jdbc.x.rs.ColumnReader;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.sql.Types.*;
import static java.time.ZoneId.systemDefault;
import static java.util.Map.Entry;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;

public final class LocalDateReaderLookup extends AbstractColumnReaderLookup<LocalDate> {

  @Override
  List<Entry<Integer, ColumnReader<?, LocalDate>>> getColumnReaders() {
    List<Entry<Integer, ColumnReader<?, LocalDate>>> entries = new ArrayList<>();
    entries.add(entry(GET_DATE, sqlDateToLocalDate(), DATE));
    entries.add(entry(GET_TIMESTAMP, timestampToLocalDate(), TIMESTAMP));
    entries.add(entry(GET_LONG, longToLocalDate(), BIGINT));
    entries.addAll(entries(GET_STRING, stringToLocalDate(), VARCHAR, CHAR));
    return entries;
  }

  private static Function<Date, LocalDate> sqlDateToLocalDate() {
    return x -> x == null ? null : x.toLocalDate();
  }

  private static Function<Timestamp, LocalDate> timestampToLocalDate() {
    return x -> x == null ? null : x.toLocalDateTime().toLocalDate();
  }

  private static Function<Long, LocalDate> longToLocalDate() {
    return x -> x == null ? null : asEpochMillis(x);
  }

  private static Function<String, LocalDate> stringToLocalDate() {
    return x -> x == null ? null : LocalDate.parse(x);
  }

  private static LocalDate asEpochMillis(Long x) {
    return Instant.ofEpochSecond(x).atZone(systemDefault()).toLocalDate();
  }


}
