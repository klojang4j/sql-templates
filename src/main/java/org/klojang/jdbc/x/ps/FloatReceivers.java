package org.klojang.jdbc.x.ps;

import org.klojang.convert.Bool;
import org.klojang.convert.NumberMethods;

import static java.sql.Types.*;

class FloatReceivers extends ReceiverLookup<Float> {

  static final Receiver<Float, ?> DEFAULT = new Receiver<>(PsSetter.SET_FLOAT);

  FloatReceivers() {
    put(REAL, DEFAULT);
    putMultiple(new Receiver<Float, Double>(PsSetter.SET_DOUBLE, NumberMethods::convert), FLOAT, DOUBLE);
    put(BIGINT, new Receiver<Float, Long>(PsSetter.SET_LONG, NumberMethods::convert));
    put(INTEGER, new Receiver<Float, Integer>(PsSetter.SET_INT, NumberMethods::convert));
    put(SMALLINT, new Receiver<Float, Short>(PsSetter.SET_SHORT, NumberMethods::convert));
    put(TINYINT, new Receiver<Float, Byte>(PsSetter.SET_BYTE, NumberMethods::convert));
    putMultiple(new Receiver<Float, Boolean>(PsSetter.SET_BOOLEAN, Bool::from), BOOLEAN, BIT);
    put(VARCHAR, Receiver.ANY_TO_STRING);
    put(CHAR, Receiver.ANY_TO_STRING);
  }

  @Override
  Receiver<Float, ?> getDefaultReceiver() {
    return DEFAULT;
  }
}
