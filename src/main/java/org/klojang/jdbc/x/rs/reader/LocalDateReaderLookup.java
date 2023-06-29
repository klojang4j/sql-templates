package org.klojang.jdbc.x.rs.reader;

import org.klojang.jdbc.x.rs.ResultSetMethod;
import org.klojang.jdbc.x.rs.ColumnReader;
import org.klojang.jdbc.x.rs.ColumnReaderLookup;

import java.sql.Date;
import java.time.LocalDate;
import static java.sql.Types.DATE;
import static java.sql.Types.TIMESTAMP;
import static org.klojang.util.ObjectMethods.ifNotNull;

public final class LocalDateReaderLookup extends ColumnReaderLookup<LocalDate> {

  public LocalDateReaderLookup() {
    add(DATE, new ColumnReader<>(ResultSetMethod.GET_DATE, x -> ifNotNull(x, Date::toLocalDate)));
    add(TIMESTAMP, new ColumnReader<>(ResultSetMethod.objectGetter(LocalDate.class)));
  }
}
