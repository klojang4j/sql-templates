package org.klojang.jdbc.x.rs;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import org.klojang.check.Check;
import org.klojang.jdbc.SQLTypeNames;

import static java.sql.Types.*;
import static org.klojang.check.CommonChecks.keyIn;

class RsMethods {

  private static RsMethods INSTANCE;

  static RsMethods getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new RsMethods();
    }
    return INSTANCE;
  }

  private final Map<Integer, RsMethod<?>> cache;

  private RsMethods() {
    cache = createCache();
  }

  @SuppressWarnings("unchecked")
  <T> RsMethod<T> getMethod(Integer sqlType) {
    // This implicitly checks that the specified int is one of the
    // static final int constants in the java.sql.Types class
    String typeName = SQLTypeNames.getTypeName(sqlType);
    Check.that(sqlType).is(keyIn(), cache, "Unsupported SQL type: %s", typeName);
    return (RsMethod<T>) cache.get(sqlType);
  }

  private static Map<Integer, RsMethod<?>> createCache() {
    Map<Integer, RsMethod<?>> tmp = new HashMap<>();
    tmp.put(VARCHAR, RsMethod.GET_STRING);
    tmp.put(LONGVARCHAR, RsMethod.GET_STRING);
    tmp.put(NVARCHAR, RsMethod.GET_STRING);
    tmp.put(LONGNVARCHAR, RsMethod.GET_STRING);
    tmp.put(CHAR, RsMethod.GET_STRING);
    tmp.put(CLOB, RsMethod.GET_STRING);

    tmp.put(INTEGER, RsMethod.GET_INT);
    tmp.put(SMALLINT, RsMethod.GET_SHORT);
    tmp.put(TINYINT, RsMethod.GET_BYTE);
    tmp.put(BIT, RsMethod.GET_BYTE);
    tmp.put(DOUBLE, RsMethod.GET_DOUBLE);
    tmp.put(REAL, RsMethod.GET_DOUBLE);
    tmp.put(FLOAT, RsMethod.GET_FLOAT);
    tmp.put(BIGINT, RsMethod.GET_LONG);

    tmp.put(BOOLEAN, RsMethod.GET_BOOLEAN);

    tmp.put(DATE, RsMethod.GET_DATE);
    tmp.put(TIME, RsMethod.GET_TIME);

    tmp.put(TIMESTAMP, RsMethod.objectGetter(LocalDateTime.class));
    tmp.put(TIMESTAMP_WITH_TIMEZONE, RsMethod.objectGetter(OffsetDateTime.class));

    tmp.put(NUMERIC, RsMethod.GET_BIG_DECIMAL);
    tmp.put(DECIMAL, RsMethod.GET_BIG_DECIMAL);

    tmp.put(ARRAY, RsMethod.objectGetter(Object[].class));
    return Map.copyOf(tmp);
  }
}
