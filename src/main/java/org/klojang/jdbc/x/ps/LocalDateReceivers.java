package org.klojang.jdbc.x.ps;

import java.sql.Date;
import java.time.LocalDate;
import static java.sql.Types.DATE;
import static java.sql.Types.TIMESTAMP;
import static org.klojang.util.ObjectMethods.ifNotNull;
import static org.klojang.jdbc.x.ps.PsSetter.SET_DATE;
import static org.klojang.jdbc.x.ps.PsSetter.setObject;

public class LocalDateReceivers extends ReceiverLookup<LocalDate> {

  static final Receiver<LocalDate, Date> DEFAULT =
      new Receiver<>(SET_DATE, d -> ifNotNull(d, Date::valueOf));

  LocalDateReceivers() {
    put(DATE, DEFAULT);
    put(TIMESTAMP, new Receiver<LocalDate, Object>(setObject(TIMESTAMP)));
  }

  @Override
  Receiver<LocalDate, ?> getDefaultReceiver() {
    return DEFAULT;
  }
}
