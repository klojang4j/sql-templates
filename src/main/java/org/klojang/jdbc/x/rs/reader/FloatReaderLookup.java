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

public final class FloatReaderLookup extends AbstractColumnReaderLookup<Float> {

  @Override
  List<Entry<Integer, ColumnReader<?, Float>>> getColumnReaders() {
    List<Entry<Integer, ColumnReader<?, Float>>> entries = new ArrayList<>(16);
    // Confusingly, Types.REAL corresponds to the Java float type while both
    // Types.DOUBLE and Types.FLOAT correspond to the Java double type. Types.NUMERIC
    // and Types.DECIMAL both correspond to BigDecimal.
    entries.add(entry(GET_FLOAT, REAL));
    entries.add(entry(GET_BOOLEAN, boolToFloat(), BOOLEAN));
    entries.add(entry(GET_LONG, longToFloat(), BIGINT));
    entries.addAll(entries(GET_INT, intToFloat(), INTEGER, SMALLINT, TINYINT));
    entries.addAll(entries(GET_DOUBLE, doubleToFloat(), DOUBLE, FLOAT));
    entries.addAll(entries(GET_BIG_DECIMAL, bdToFloat(), NUMERIC, DECIMAL));
    entries.addAll(entries(GET_STRING, stringToFloat(), VARCHAR, CHAR));
    return entries;
  }

  private static Function<Boolean, Float> boolToFloat() {
    return x -> x == null || !x ? 0F : 1F;
  }

  private static Function<Integer, Float> intToFloat() {
    return Integer::floatValue;
  }

  private static Function<Long, Float> longToFloat() {
    return x -> convert(x, Float.class);
  }

  private static Function<Double, Float> doubleToFloat() {
    return x -> convert(x, Float.class);
  }

  private static Function<BigDecimal, Float> bdToFloat() {
    return x -> convert(x, Float.class);
  }

  private static Function<String, Float> stringToFloat() {
    return x -> parse(x, Float.class);
  }

}
