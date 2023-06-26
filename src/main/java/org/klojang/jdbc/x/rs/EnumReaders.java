package org.klojang.jdbc.x.rs;

import org.klojang.convert.NumberMethods;
import org.klojang.jdbc.KJSQLException;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;

final class EnumReaders extends ReaderLookup<Enum<?>> {

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

  EnumReaders() {
    add(BIGINT, new ResultSetReader<>(GET_LONG, new NumberAdapter<Long>()));
    add(INTEGER, new ResultSetReader<>(GET_INT, new NumberAdapter<Integer>()));
    add(SMALLINT, new ResultSetReader<>(GET_SHORT, new NumberAdapter<Short>()));
    add(TINYINT, new ResultSetReader<>(GET_BYTE, new NumberAdapter<Byte>()));
    addMultiple(new ResultSetReader<>(GET_STRING, new StringAdapter()), CHAR, VARCHAR);
  }

  private static <T extends Number> Enum<?> asOrdinal(T number,
        Class<Enum<?>> enumClass) {
    if (number == null) {
      return null;
    }
    int i = number.intValue();
    if (i < 0 || i >= enumClass.getEnumConstants().length) {
      String fmt = "Invalid ordinal number for enum type %s: %d";
      String msg = String.format(fmt, enumClass.getSimpleName(), i);
      throw new KJSQLException(msg);
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
    String fmt = "Unable to parse \"%s\" into %s";
    String msg = String.format(fmt, s, enumClass.getSimpleName());
    throw new KJSQLException(msg);
  }

}
