package org.klojang.jdbc.x.rs;

/*
 * Converts a value retrieved through one of the ResultSet.getXXX() methods such that it
 * can be assigned to a JavaBean property.
 */
@FunctionalInterface
public interface Adapter<COLUMN_TYPE, FIELD_TYPE> {

  FIELD_TYPE adapt(COLUMN_TYPE value, Class<FIELD_TYPE> targetType);
}
