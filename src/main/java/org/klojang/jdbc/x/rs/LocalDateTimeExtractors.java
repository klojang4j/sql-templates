package org.klojang.jdbc.x.rs;

import java.time.LocalDateTime;
import static java.sql.Types.DATE;
import static java.sql.Types.TIMESTAMP;

class LocalDateTimeExtractors extends ExtractorLookup<LocalDateTime> {

  LocalDateTimeExtractors() {
    add(DATE, new RsExtractor<>(RsMethod.GET_DATE, d -> d == null ? null : d.toLocalDate().atStartOfDay()));
    add(TIMESTAMP, new RsExtractor<>(RsMethod.GET_TIMESTAMP, d -> d == null ? null : d.toLocalDateTime()));
  }
}
