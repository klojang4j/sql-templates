package org.klojang.jdbc.x.rs;

import java.sql.ResultSet;

/**
 * Copies a single value from a ResultSet to a map or JavaBean.
 */
interface Channel<T> {

   void copy(ResultSet rs, T target) throws Throwable;

  int getSqlType();
}
