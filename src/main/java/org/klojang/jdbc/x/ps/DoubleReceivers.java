package org.klojang.jdbc.x.ps;

import org.klojang.convert.Bool;
import org.klojang.convert.NumberMethods;

import static java.sql.Types.*;

class DoubleReceivers extends ReceiverLookup<Double> {

  static final Receiver<Double, ?> DEFAULT = new Receiver<>(PsSetter.SET_DOUBLE);

  DoubleReceivers() {
    put(FLOAT, DEFAULT);
    put(DOUBLE, DEFAULT);
    put(BIGINT, new Receiver<Double, Long>(PsSetter.SET_LONG, NumberMethods::convert));
    put(REAL, new Receiver<Double, Float>(PsSetter.SET_FLOAT, NumberMethods::convert));
    put(INTEGER, new Receiver<Double, Integer>(PsSetter.SET_INT, NumberMethods::convert));
    put(SMALLINT, new Receiver<Double, Short>(PsSetter.SET_SHORT, NumberMethods::convert));
    put(TINYINT, new Receiver<Double, Byte>(PsSetter.SET_BYTE, NumberMethods::convert));
    putMultiple(new Receiver<Double, Boolean>(PsSetter.SET_BOOLEAN, Bool::from), BOOLEAN, BIT);
    put(VARCHAR, Receiver.ANY_TO_STRING);
    put(CHAR, Receiver.ANY_TO_STRING);
  }

  @Override
  Receiver<Double, ?> getDefaultReceiver() {
    return DEFAULT;
  }
}
