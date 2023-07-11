package org.klojang.jdbc.x;

public final class SQLExpression {

    private final String expression;

    public SQLExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {return expression;}
}
