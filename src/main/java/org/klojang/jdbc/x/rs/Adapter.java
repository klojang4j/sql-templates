package org.klojang.jdbc.x.rs;

/**
 * A conversion function used to adapt the value and/or type of a database value to its
 * destination (e.g. a bean property or a record component).
 *
 * @param <COLUMN_TYPE> the column type
 * @param <TARGET_TYPE> the required type
 * @see ColumnReader
 */
@FunctionalInterface
public interface Adapter<COLUMN_TYPE, TARGET_TYPE> {

  TARGET_TYPE adapt(COLUMN_TYPE value, Class<TARGET_TYPE> targetType);
}
