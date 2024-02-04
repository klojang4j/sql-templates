package org.klojang.jdbc.x.rs.reader;

import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.DatabaseException;
import org.klojang.jdbc.x.rs.Adapter;
import org.klojang.jdbc.x.rs.ColumnReader;
import org.klojang.jdbc.x.rs.ColumnReaderLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;

public final class EnumReaderLookup extends ColumnReaderLookup<Enum<?>> {

  public EnumReaderLookup() {
    add(BIGINT, new ColumnReader<>(GET_LONG, new NumberAdapter<>()));
    add(INTEGER, new ColumnReader<>(GET_INT, new NumberAdapter<>()));
    add(SMALLINT, new ColumnReader<>(GET_SHORT, new NumberAdapter<>()));
    add(TINYINT, new ColumnReader<>(GET_BYTE, new NumberAdapter<>()));
    addMultiple(new ColumnReader<>(GET_STRING, new StringAdapter()), CHAR, VARCHAR);
  }

  private static class NumberAdapter<T extends Number> implements
        Adapter<T, Enum<?>> {

    @Override
    public Enum<?> adapt(T i, Class<Enum<?>> t) {
      return asOrdinal(i, t);
    }

  }

  private static class StringAdapter implements Adapter<String, Enum<?>> {

    @Override
    public Enum<?> adapt(String s, Class<Enum<?>> t) {
      int i;
      try {
        i = NumberMethods.parse(s, Integer.class);
      } catch (IllegalArgumentException e) {
        return asName(s, t);
      }
      return asOrdinal(i, t);
    }

  }


  private static <T extends Number> Enum<?> asOrdinal(T number,
        Class<Enum<?>> enumClass) {
    if (number == null) {
      return null;
    }
    int i = number.intValue();
    if (i < 0 || i >= enumClass.getEnumConstants().length) {
      String fmt = "invalid ordinal number for enum type %s: %d";
      String msg = String.format(fmt, enumClass.getSimpleName(), i);
      throw new DatabaseException(msg);
    }
    return enumClass.getEnumConstants()[i];
  }

  private static Enum<?> asName(String s, Class<Enum<?>> enumClass) {
    if (s == null) {
      return null;
    }
    for (Enum<?> c : enumClass.getEnumConstants()) {
      if (s.equals(c.name()) || s.equals(c.toString())) {
        return c;
      }
    }
    String fmt = "unable to parse \"%s\" into %s";
    String msg = String.format(fmt, s, enumClass.getSimpleName());
    throw new DatabaseException(msg);
  }

}
