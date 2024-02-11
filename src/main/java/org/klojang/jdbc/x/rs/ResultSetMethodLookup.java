package org.klojang.jdbc.x.rs;

import org.klojang.check.Check;
import org.klojang.jdbc.util.SQLTypeUtil;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.sql.Types.*;
import static org.klojang.check.CommonChecks.keyIn;

/**
 * Maps SQL types (the static final int constants of java.sql.SQLType) to ResultSetMethod
 * instances. Used when copying values from a ResultSet to a Map. For maps, values do not
 * have type restrictions.
 */
final class ResultSetMethodLookup {

  private static ResultSetMethodLookup INSTANCE;

  static ResultSetMethodLookup getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ResultSetMethodLookup();
    }
    return INSTANCE;
  }

  private final Map<Integer, ResultSetMethod<?>> cache;

  private ResultSetMethodLookup() {
    cache = createCache();
  }

  @SuppressWarnings("unchecked")
  <T> ResultSetMethod<T> getMethod(Integer sqlType) {
    // This implicitly checks that the specified int is one of the
    // static final int constants in the java.sql.Types class
    String typeName = SQLTypeUtil.getTypeName(sqlType);
    Check.that(sqlType).is(keyIn(), cache, "Unsupported SQL type: %s", typeName);
    return (ResultSetMethod<T>) cache.get(sqlType);
  }

  private static Map<Integer, ResultSetMethod<?>> createCache() {

    Map<Integer, ResultSetMethod<?>> tmp = new HashMap<>();

    tmp.put(VARCHAR, ResultSetMethod.GET_STRING);
    tmp.put(LONGVARCHAR, ResultSetMethod.GET_STRING);
    tmp.put(NVARCHAR, ResultSetMethod.GET_STRING);
    tmp.put(LONGNVARCHAR, ResultSetMethod.GET_STRING);
    tmp.put(CHAR, ResultSetMethod.GET_STRING);
    tmp.put(CLOB, ResultSetMethod.GET_STRING);

    tmp.put(INTEGER, ResultSetMethod.GET_INT);
    tmp.put(SMALLINT, ResultSetMethod.GET_SHORT);
    tmp.put(TINYINT, ResultSetMethod.GET_BYTE);
    // Confusingly, Types.REAL corresponds to the Java float type while both
    // Types.DOUBLE and Types.FLOAT correspond to the Java double type.
    tmp.put(DOUBLE, ResultSetMethod.GET_DOUBLE);
    tmp.put(FLOAT, ResultSetMethod.GET_DOUBLE);
    tmp.put(REAL, ResultSetMethod.GET_FLOAT);
    tmp.put(BIGINT, ResultSetMethod.GET_LONG);

    tmp.put(BOOLEAN, ResultSetMethod.GET_BOOLEAN);

    tmp.put(DATE, ResultSetMethod.GET_DATE);
    tmp.put(TIME, ResultSetMethod.GET_TIME);

    tmp.put(TIMESTAMP, ResultSetMethod.getObjectGetter(LocalDateTime.class));
    tmp.put(TIMESTAMP_WITH_TIMEZONE, ResultSetMethod.getObjectGetter(OffsetDateTime.class));

    tmp.put(NUMERIC, ResultSetMethod.GET_BIG_DECIMAL);
    tmp.put(DECIMAL, ResultSetMethod.GET_BIG_DECIMAL);

    tmp.put(BINARY, ResultSetMethod.GET_BYTES);

    tmp.put(ARRAY, ResultSetMethod.getObjectGetter(Object[].class));

    return Map.copyOf(tmp);
  }
}
