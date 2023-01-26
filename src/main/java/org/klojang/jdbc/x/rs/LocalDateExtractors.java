package org.klojang.jdbc.x.rs;

import java.sql.Date;
import java.time.LocalDate;
import static java.sql.Types.DATE;
import static java.sql.Types.TIMESTAMP;
import static org.klojang.util.ObjectMethods.ifNotNull;

class LocalDateExtractors extends ExtractorLookup<LocalDate> {

  LocalDateExtractors() {
    add(DATE, new RsExtractor<>(RsMethod.GET_DATE, x -> ifNotNull(x, Date::toLocalDate)));
    add(TIMESTAMP, new RsExtractor<>(RsMethod.objectGetter(LocalDate.class)));
  }
}
