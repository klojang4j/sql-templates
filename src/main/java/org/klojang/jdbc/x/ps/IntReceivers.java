package org.klojang.jdbc.x.ps;

import org.klojang.convert.Bool;
import org.klojang.convert.NumberMethods;

import static java.sql.Types.*;

class IntReceivers extends ReceiverLookup<Integer> {

  static final Receiver<Integer, Integer> DEFAULT = new Receiver<>(PsSetter.SET_INT);

  IntReceivers() {
    put(INTEGER, DEFAULT);
    put(SMALLINT, new Receiver<Integer, Short>(PsSetter.SET_SHORT, NumberMethods::convert));
    put(TINYINT, new Receiver<Integer, Byte>(PsSetter.SET_BYTE, NumberMethods::convert));
    putMultiple(new Receiver<Integer, Boolean>(PsSetter.SET_BOOLEAN, Bool::from), BOOLEAN, BIT);
    put(VARCHAR, Receiver.ANY_TO_STRING);
    put(CHAR, Receiver.ANY_TO_STRING);
  }

  @Override
  Receiver<Integer, ?> getDefaultReceiver() {
    return DEFAULT;
  }
}
