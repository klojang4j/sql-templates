package org.klojang.jdbc.x.rs.reader;

import org.klojang.jdbc.x.rs.ColumnReader;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.sql.Types.*;
import static java.util.Map.Entry;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;

public final class BigDecimalReaderLookup extends AbstractColumnReaderLookup<BigDecimal> {

  @Override
  List<Entry<Integer, ColumnReader<?, BigDecimal>>> getColumnReaders() {
    List<Entry<Integer, ColumnReader<?, BigDecimal>>> entries = new ArrayList<>(16);
    // We are a bit cautious. We are not sure whether every JDBC driver will be happy
    // with you calling ResultSet.getBigDecimal on tiny column types, even though the
    // conversion is easy and lossless. So just to be sure, we do the conversion
    // ourselves.
    entries.add(entry(GET_LONG, longToBigDecimal(), BIGINT));
    entries.addAll(entries(GET_INT, intToBigDecimal(), INTEGER, SMALLINT, TINYINT));
    entries.addAll(entries(GET_FLOAT, floatToBigDecimal(), REAL));
    entries.addAll(entries(GET_DOUBLE, doubleToBigDecimal(), DOUBLE, FLOAT));
    entries.addAll(entries(GET_BIG_DECIMAL, NUMERIC, DECIMAL));
    entries.addAll(entries(GET_STRING, stringToBigDecimal(), VARCHAR, CHAR));
    return entries;
  }

  private static Function<Integer, BigDecimal> intToBigDecimal() {
    return x -> x == null ? null : new BigDecimal(x);
  }

  private static Function<Long, BigDecimal> longToBigDecimal() {
    return x -> x == null ? null : BigDecimal.valueOf(x);
  }

  private static Function<Float, BigDecimal> floatToBigDecimal() {
    return x -> x == null ? null : BigDecimal.valueOf(x);
  }

  private static Function<Double, BigDecimal> doubleToBigDecimal() {
    return x -> x == null ? null : BigDecimal.valueOf(x);
  }

  private static Function<String, BigDecimal> stringToBigDecimal() {
    return x -> x == null ? null : new BigDecimal(x);
  }
}
