package org.klojang.jdbc.x.sql;

public record SQLExpression(String expression) {

  @Override
  public String toString() { return expression; }
}
