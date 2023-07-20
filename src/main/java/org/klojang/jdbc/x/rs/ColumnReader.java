package org.klojang.jdbc.x.rs;

import java.sql.ResultSet;
import java.util.function.Function;

import static org.klojang.util.ObjectMethods.ifNotNull;

/*
 * Extracts a single value from a ResultSet, possibly after first converting it to the
 * appropriate type.
 */
public final class ColumnReader<COLUMN_TYPE, FIELD_TYPE> {

  private final ResultSetMethod<COLUMN_TYPE> method;
  private final Adapter<? super COLUMN_TYPE, FIELD_TYPE> adapter;

  public ColumnReader(ResultSetMethod<COLUMN_TYPE> method) {
    this.method = method;
    this.adapter = null;
  }

  public ColumnReader(
        ResultSetMethod<COLUMN_TYPE> method,
        Function<? super COLUMN_TYPE, FIELD_TYPE> adapter) {
    this(method, (x, y) -> ifNotNull(x, adapter::apply));
  }

  public ColumnReader(
        ResultSetMethod<COLUMN_TYPE> method,
        Adapter<? super COLUMN_TYPE, FIELD_TYPE> adapter) {
    this.method = method;
    this.adapter = adapter;
  }

  @SuppressWarnings("unchecked")
  public FIELD_TYPE getValue(ResultSet rs, int columnIndex, Class<FIELD_TYPE> toType)
  throws Throwable {
    COLUMN_TYPE val = method.invoke(rs, columnIndex);
    if (adapter == null) {
      return (FIELD_TYPE) val;
    }
    return adapter.adapt(val, toType);
  }

}
