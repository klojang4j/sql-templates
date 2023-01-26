package org.klojang.jdbc.x.ps;

import static java.sql.Types.*;

class EnumReceivers extends ReceiverLookup<Enum<?>> {

  static final Receiver<Enum<?>, Integer> DEFAULT = new Receiver<>(PsSetter.SET_INT, Enum::ordinal);

  static final Receiver<Enum<?>, String> ENUM_TO_STRING =
      new Receiver<>(PsSetter.SET_STRING, Object::toString);

  EnumReceivers() {
    put(INTEGER, DEFAULT);
    put(BIGINT, new Receiver<Enum<?>, Long>(PsSetter.SET_LONG, e -> (long) e.ordinal()));
    put(SMALLINT, new Receiver<Enum<?>, Short>(PsSetter.SET_SHORT, e -> (short) e.ordinal()));
    put(TINYINT, new Receiver<Enum<?>, Byte>(PsSetter.SET_BYTE, e -> (byte) e.ordinal()));
    put(VARCHAR, ENUM_TO_STRING);
    put(CHAR, ENUM_TO_STRING);
  }

  @Override
  Receiver<Enum<?>, ?> getDefaultReceiver() {
    return DEFAULT;
  }
}
