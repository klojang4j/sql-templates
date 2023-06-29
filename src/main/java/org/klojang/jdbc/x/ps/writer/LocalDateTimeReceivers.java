package org.klojang.jdbc.x.ps.writer;

import org.klojang.jdbc.x.ps.ColumnWriter;
import org.klojang.jdbc.x.ps.ColumnWriterLookup;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static java.sql.Types.TIMESTAMP;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.SET_TIMESTAMP;
import static org.klojang.util.ObjectMethods.ifNotNull;

public final class LocalDateTimeReceivers extends ColumnWriterLookup<LocalDateTime> {

  public static final ColumnWriter<LocalDateTime, Timestamp> DEFAULT =
        new ColumnWriter<>(SET_TIMESTAMP, d -> ifNotNull(d, Timestamp::valueOf));

  public LocalDateTimeReceivers() {
    put(TIMESTAMP, DEFAULT);
  }


}
