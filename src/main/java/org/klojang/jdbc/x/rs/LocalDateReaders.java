package org.klojang.jdbc.x.rs;

import java.sql.Date;
import java.time.LocalDate;
import static java.sql.Types.DATE;
import static java.sql.Types.TIMESTAMP;
import static org.klojang.util.ObjectMethods.ifNotNull;

final class LocalDateReaders extends ReaderLookup<LocalDate> {

  LocalDateReaders() {
    add(DATE, new ResultSetReader<>(ResultSetMethod.GET_DATE, x -> ifNotNull(x, Date::toLocalDate)));
    add(TIMESTAMP, new ResultSetReader<>(ResultSetMethod.objectGetter(LocalDate.class)));
  }
}
