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

public final class ByteReaderLookup extends AbstractColumnReaderLookup<Byte> {

  @Override
  List<Entry<Integer, ColumnReader<?, Byte>>> getColumnReaders() {
    List<Entry<Integer, ColumnReader<?, Byte>>> entries = new ArrayList<>(16);
    entries.add(entry(GET_BYTE, TINYINT));
    entries.add(entry(GET_BOOLEAN, boolToByte(), BOOLEAN));
    entries.add(entry(GET_FLOAT, floatToByte(), REAL));
    entries.add(entry(GET_LONG, longToByte(), BIGINT));
    entries.addAll(entries(GET_INT, intToByte(), INTEGER, SMALLINT));
    entries.addAll(entries(GET_DOUBLE, doubleToByte(), FLOAT, DOUBLE));
    entries.addAll(entries(GET_BIG_DECIMAL, bdToByte(), NUMERIC, DECIMAL));
    entries.addAll(entries(GET_STRING, stringToByte(), VARCHAR, CHAR));
    return entries;
  }

  private static Function<Boolean, Byte> boolToByte() {
    return x -> (byte) (x ? 1 : 0);
  }

  private static Function<Integer, Byte> intToByte() {
    return x -> convert(x, Byte.class);
  }

  private static Function<Float, Byte> floatToByte() {
    return x -> convert(x, Byte.class);
  }

  private static Function<Long, Byte> longToByte() {
    return x -> convert(x, Byte.class);
  }

  private static Function<Double, Byte> doubleToByte() {
    return x -> convert(x, Byte.class);
  }

  private Function<BigDecimal, Byte> bdToByte() {
    return x -> convert(x, Byte.class);
  }

  private static Function<String, Byte> stringToByte() {
    return x -> parse(x, Byte.class);
  }

}
