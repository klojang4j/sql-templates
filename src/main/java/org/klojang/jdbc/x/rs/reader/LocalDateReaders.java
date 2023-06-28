package org.klojang.jdbc.x.rs.reader;

import org.klojang.jdbc.x.rs.ResultSetMethod;
import org.klojang.jdbc.x.rs.ResultSetReader;
import org.klojang.jdbc.x.rs.ResultSetReaderLookup;

import java.sql.Date;
import java.time.LocalDate;
import static java.sql.Types.DATE;
import static java.sql.Types.TIMESTAMP;
import static org.klojang.util.ObjectMethods.ifNotNull;

public final class LocalDateReaders extends ResultSetReaderLookup<LocalDate> {

  public LocalDateReaders() {
    add(DATE, new ResultSetReader<>(ResultSetMethod.GET_DATE, x -> ifNotNull(x, Date::toLocalDate)));
    add(TIMESTAMP, new ResultSetReader<>(ResultSetMethod.objectGetter(LocalDate.class)));
  }
}
