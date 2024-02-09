package org.klojang.jdbc.x.rs.reader;

import org.klojang.jdbc.x.rs.ColumnReader;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.sql.Types.*;
import static java.util.Map.Entry;
import static org.klojang.convert.NumberMethods.convert;
import static org.klojang.convert.NumberMethods.parse;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;

public final class IntReaderLookup extends AbstractColumnReaderLookup<Integer> {

  @Override
  List<Entry<Integer, ColumnReader<?, Integer>>> getColumnReaders() {
    List<Entry<Integer, ColumnReader<?, Integer>>> entries = new ArrayList<>(16);
    entries.add(entry(GET_BOOLEAN, boolToInt(), BOOLEAN));
    entries.add(entry(GET_FLOAT, floatToInt(), REAL));
    entries.add(entry(GET_LONG, longToInt(), BIGINT));
    entries.addAll(entries(GET_INT, INTEGER, SMALLINT, TINYINT));
    entries.addAll(entries(GET_DOUBLE, doubleToInt(), DOUBLE, FLOAT));
    entries.addAll(entries(GET_BIG_DECIMAL, bdToInt(), NUMERIC, DECIMAL));
    entries.addAll(entries(GET_STRING, stringToInt(), VARCHAR, CHAR));
    return entries;
  }

  private static Function<Boolean, Integer> boolToInt() {
    return x -> x ? 1 : 0;
  }

  private static Function<Float, Integer> floatToInt() {
    return Float::intValue;
  }

  private static Function<Long, Integer> longToInt() {
    return x -> convert(x, Integer.class);
  }

  private static Function<Double, Integer> doubleToInt() {
    return x -> convert(x, Integer.class);
  }

  private Function<BigDecimal, Integer> bdToInt() {
    return x -> convert(x, Integer.class);
  }

  private static Function<String, Integer> stringToInt() {
    return x -> parse(x, Integer.class);
  }

}
