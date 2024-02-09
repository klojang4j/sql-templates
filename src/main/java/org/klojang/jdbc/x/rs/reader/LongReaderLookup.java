package org.klojang.jdbc.x.rs.reader;

import org.klojang.jdbc.x.rs.ColumnReader;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.sql.Types.*;
import static java.time.ZoneId.systemDefault;
import static java.util.Map.Entry;
import static org.klojang.convert.NumberMethods.convert;
import static org.klojang.convert.NumberMethods.parse;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;

public final class LongReaderLookup extends AbstractColumnReaderLookup<Long> {

  @Override
  List<Entry<Integer, ColumnReader<?, Long>>> getColumnReaders() {
    List<Entry<Integer, ColumnReader<?, Long>>> entries = new ArrayList<>(16);
    entries.add(entry(GET_BOOLEAN, boolToLong(), BOOLEAN));
    entries.add(entry(GET_FLOAT, floatToLong(), REAL));
    entries.add(entry(GET_DATE, sqlDateToLong(), DATE));
    entries.add(entry(GET_TIMESTAMP, timestampLong(), TIMESTAMP));
    entries.addAll(entries(GET_LONG, BIGINT, INTEGER, SMALLINT, TINYINT));
    entries.addAll(entries(GET_DOUBLE, doubleToLong(), DOUBLE, FLOAT));
    entries.addAll(entries(GET_BIG_DECIMAL, bdToLong(), NUMERIC, DECIMAL));
    entries.addAll(entries(GET_STRING, stringToLong(), VARCHAR, CHAR));
    return entries;
  }


  private static Function<Boolean, Long> boolToLong() {
    return x -> x ? 1L : 0L;
  }

  private static Function<Float, Long> floatToLong() {
    return x -> convert(x, Long.class);
  }

  private static Function<Double, Long> doubleToLong() {
    return x -> convert(x, Long.class);
  }

  private Function<BigDecimal, Long> bdToLong() {
    return x -> convert(x, Long.class);
  }

  private static Function<String, Long> stringToLong() {
    return x -> parse(x, Long.class);
  }

  private static Function<java.sql.Date, Long> sqlDateToLong() {
    return x -> {
      if (x == null) {
        return null;
      }
      return x.toLocalDate().atStartOfDay(systemDefault()).toEpochSecond();
    };
  }

  private static Function<Timestamp, Long> timestampLong() {
    return x -> {
      if (x == null) {
        return null;
      }
      return x.toInstant().toEpochMilli();
    };
  }


}
