package org.klojang.jdbc.x.rs.reader;

import org.klojang.convert.NumberMethods;
import org.klojang.convert.TypeConversionException;
import org.klojang.jdbc.DatabaseException;
import org.klojang.jdbc.x.rs.Adapter;
import org.klojang.jdbc.x.rs.ColumnReader;

import java.util.ArrayList;
import java.util.List;

import static java.sql.Types.*;
import static java.util.Map.Entry;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;

public final class EnumReaderLookup extends AbstractColumnReaderLookup<Enum<?>> {

  @Override
  List<Entry<Integer, ColumnReader<?, Enum<?>>>> getColumnReaders() {
    List<Entry<Integer, ColumnReader<?, Enum<?>>>> entries = new ArrayList<>(16);
    entries.add(entry(GET_LONG, numberToEnum(), BIGINT));
    entries.add(entry(GET_INT, numberToEnum(), INTEGER));
    entries.add(entry(GET_SHORT, numberToEnum(), SMALLINT));
    entries.add(entry(GET_BYTE, numberToEnum(), TINYINT));
    entries.addAll(entries(GET_STRING, stringToEnum(), VARCHAR, CHAR));
    return entries;
  }

  private static <T extends Number> Adapter<T, Enum<?>> numberToEnum() {
    return (x, y) -> asOrdinal(x, y);
  }

  private static Adapter<String, Enum<?>> stringToEnum() {
    return (x, y) -> stringToEnum(x, y);
  }

  private static <T extends Number> Enum<?> asOrdinal(T n,
        Class<Enum<?>> enumClass) {
    int i = n.intValue();
    if (i < 0 || i >= enumClass.getEnumConstants().length) {
      String fmt = "invalid ordinal number for enum type %s: %d";
      String msg = String.format(fmt, enumClass.getSimpleName(), i);
      throw new DatabaseException(msg);
    }
    return enumClass.getEnumConstants()[i];
  }

  private static Enum<?> stringToEnum(String s, Class<Enum<?>> enumClass) {
    int i;
    try {
      i = NumberMethods.parse(s, Integer.class);
    } catch (TypeConversionException e) {
      return asName(s, enumClass);
    }
    return asOrdinal(i, enumClass);
  }


  private static Enum<?> asName(String s, Class<Enum<?>> enumClass) {
    for (Enum<?> c : enumClass.getEnumConstants()) {
      if (s.equals(c.name()) || s.equals(c.toString())) {
        return c;
      }
    }
    String fmt = "cannot parse \"%s\" into %s";
    String msg = String.format(fmt, s, enumClass.getSimpleName());
    throw new DatabaseException(msg);
  }

}
