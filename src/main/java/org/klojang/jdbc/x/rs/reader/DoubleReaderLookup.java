package org.klojang.jdbc.x.rs.reader;

import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.x.rs.ColumnReader;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.sql.Types.*;
import static java.util.Map.Entry;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;

public final class DoubleReaderLookup extends AbstractColumnReaderLookup<Double> {

  @Override
  List<Entry<Integer, ColumnReader<?, Double>>> getColumnReaders() {
    List<Entry<Integer, ColumnReader<?, Double>>> entries = new ArrayList<>(16);
    entries.add(entry(GET_BOOLEAN, boolToDouble(), BOOLEAN));
    entries.addAll(entries(GET_DOUBLE,
          DOUBLE,
          FLOAT,
          REAL,
          BIGINT,
          INTEGER,
          SMALLINT,
          TINYINT));
    entries.addAll(entries(GET_BIG_DECIMAL, bigDecimalToDouble(), NUMERIC, DECIMAL));
    entries.addAll(entries(GET_STRING, stringToDouble(), VARCHAR, CHAR));
    return entries;
  }

  private Function<Boolean, Double> boolToDouble() {
    return x -> x ? 1D : 0D;
  }

  private static Function<BigDecimal, Double> bigDecimalToDouble() {
    return x -> NumberMethods.convert(x, Double.class);
  }

  private static Function<String, Double> stringToDouble() {
    return x -> NumberMethods.parse(x, Double.class);
  }

}
