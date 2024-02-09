package org.klojang.jdbc.x.rs.reader;

import org.klojang.jdbc.x.rs.ColumnReader;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.sql.Types.*;
import static org.klojang.convert.NumberMethods.convert;
import static org.klojang.convert.NumberMethods.parse;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;
import static java.util.Map.Entry;

public final class ShortReaderLookup extends AbstractColumnReaderLookup<Short> {

  @Override
  List<Entry<Integer, ColumnReader<?, Short>>> getColumnReaders() {
    List<Entry<Integer, ColumnReader<?, Short>>> entries = new ArrayList<>(16);
    entries.add(entry(GET_BOOLEAN, boolToShort(), BOOLEAN));
    entries.add(entry(GET_INT, intToShort(), INTEGER));
    entries.add(entry(GET_LONG, longToShort(), BIGINT));
    entries.add(entry(GET_FLOAT, floatToShort(), REAL));
    entries.addAll(entries(GET_SHORT, SMALLINT, TINYINT));
    entries.addAll(entries(GET_DOUBLE, doubleToShort(), FLOAT, DOUBLE));
    entries.addAll(entries(GET_BIG_DECIMAL, bdToShort(), NUMERIC, DECIMAL));
    entries.addAll(entries(GET_STRING, stringToShort(), VARCHAR, CHAR));
    return entries;
  }

  private static Function<Boolean, Short> boolToShort() {
    return x -> (short) (x ? 1 : 0);
  }

  private static Function<Short, Short> intToShort() {
    return x -> convert(x, Short.class);
  }

  private static Function<Float, Short> floatToShort() {
    return x -> convert(x, Short.class);
  }

  private static Function<Long, Short> longToShort() {
    return x -> convert(x, Short.class);
  }

  private static Function<Double, Short> doubleToShort() {
    return x -> convert(x, Short.class);
  }

  private Function<BigDecimal, Short> bdToShort() {
    return x -> convert(x, Short.class);
  }

  private static Function<String, Short> stringToShort() {
    return x -> parse(x, Short.class);
  }


}
