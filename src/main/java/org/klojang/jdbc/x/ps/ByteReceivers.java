package org.klojang.jdbc.x.ps;

import org.klojang.convert.Bool;

import static java.sql.Types.*;

class ByteReceivers extends ReceiverLookup<Byte> {

  static final Receiver<Byte, Byte> DEFAULT = new Receiver<>(PsSetter.SET_BYTE);

  ByteReceivers() {
    put(TINYINT, DEFAULT);
    put(INTEGER, IntReceivers.DEFAULT);
    put(SMALLINT, new Receiver<>(PsSetter.SET_SHORT));
    put(BIGINT, new Receiver<>(PsSetter.SET_LONG));
    put(REAL, new Receiver<>(PsSetter.SET_FLOAT));
    put(FLOAT, DoubleReceivers.DEFAULT);
    put(DOUBLE, DoubleReceivers.DEFAULT);
    put(NUMERIC, new Receiver<>(PsSetter.SET_BIG_DECIMAL));
    put(DECIMAL, new Receiver<>(PsSetter.SET_BIG_DECIMAL));
    put(BOOLEAN, new Receiver<Byte, Boolean>(PsSetter.SET_BOOLEAN, Bool::from));
    put(BIT, new Receiver<Byte, Boolean>(PsSetter.SET_BOOLEAN, Bool::from));
    put(VARCHAR, Receiver.ANY_TO_STRING);
    put(CHAR, Receiver.ANY_TO_STRING);
  }

  @Override
  Receiver<Byte, ?> getDefaultReceiver() {
    return DEFAULT;
  }
}
