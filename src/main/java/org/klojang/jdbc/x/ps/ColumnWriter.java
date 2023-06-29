package org.klojang.jdbc.x.ps;

import java.sql.PreparedStatement;
import java.util.function.Function;

import static org.klojang.util.ObjectMethods.ifNotEmpty;

/*
 * Binds a single value to a PreparedStatement, possibly after first converting it to the
 * appropriate type.
 */
public final class ColumnWriter<FIELD_TYPE, PARAM_TYPE> {

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static final ColumnWriter<?, String> ANY_TO_STRING
        = new ColumnWriter(PreparedStatementMethod.SET_STRING, String::valueOf);

  private final PreparedStatementMethod<PARAM_TYPE> setter;
  private final Adapter<FIELD_TYPE, PARAM_TYPE> adapter;

  public ColumnWriter(PreparedStatementMethod<PARAM_TYPE> setter) {
    this.setter = setter;
    this.adapter = null;
  }

  public ColumnWriter(PreparedStatementMethod<PARAM_TYPE> setter,
        Function<FIELD_TYPE, PARAM_TYPE> adapter) {
    this(setter, (x, y) -> ifNotEmpty(x, adapter));
  }

  public ColumnWriter(PreparedStatementMethod<PARAM_TYPE> setter,
        Adapter<FIELD_TYPE, PARAM_TYPE> adapter) {
    this.setter = setter;
    this.adapter = adapter;
  }

  @SuppressWarnings("unchecked")
  PARAM_TYPE getParamValue(FIELD_TYPE beanValue) {
    return adapter == null
          ? (PARAM_TYPE) beanValue
          : adapter.adapt(beanValue, setter.getParamType());
  }

  void bind(PreparedStatement ps, int paramIndex, PARAM_TYPE value) throws Throwable {
    setter.bindValue(ps, paramIndex, value);
  }
}
