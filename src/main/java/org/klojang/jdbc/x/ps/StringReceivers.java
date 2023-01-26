package org.klojang.jdbc.x.ps;

import org.klojang.convert.Bool;
import org.klojang.convert.NumberMethods;

import java.math.BigDecimal;

import static java.sql.Types.*;

class StringReceivers extends ReceiverLookup<String> {

  static final Receiver<String, String> DEFAULT = new Receiver<>(PsSetter.SET_STRING);

  StringReceivers() {
    put(VARCHAR, DEFAULT);
    put(CHAR, DEFAULT);
    put(INTEGER, new Receiver<String, Integer>(PsSetter.SET_INT, NumberMethods::parse));
    put(SMALLINT, new Receiver<String, Short>(PsSetter.SET_SHORT, NumberMethods::parse));
    put(TINYINT, new Receiver<String, Byte>(PsSetter.SET_BYTE, NumberMethods::parse));
    put(BIGINT, new Receiver<String, Long>(PsSetter.SET_LONG, NumberMethods::parse));
    put(NUMERIC,
        new Receiver<String, BigDecimal>(PsSetter.SET_BIG_DECIMAL, NumberMethods::parse));
    put(REAL, new Receiver<String, Float>(PsSetter.SET_FLOAT, NumberMethods::parse));
    put(FLOAT, new Receiver<String, Double>(PsSetter.SET_DOUBLE, NumberMethods::parse));
    put(DOUBLE, new Receiver<String, Double>(PsSetter.SET_DOUBLE, NumberMethods::parse));
    put(BOOLEAN, new Receiver<String, Boolean>(PsSetter.SET_BOOLEAN, Bool::from));
    put(BIT, new Receiver<Integer, Boolean>(PsSetter.SET_BOOLEAN, Bool::from));
  }

  @Override
  Receiver<String, ?> getDefaultReceiver() {
    return DEFAULT;
  }

}
