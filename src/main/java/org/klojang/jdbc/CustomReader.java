package org.klojang.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <p>A {@code CustomReader} gives you full control over how a value is extracted from a
 * {@link ResultSet}. It simply hands you the {@code ResultSet} and lets you extract the
 * value yourself. If the value is to be assigned to a JavaBean property or {@code record}
 * component, it is your responsibility to ensure the value is type-compatible with the
 * property or component, or a {@link ClassCastException} will follow.
 *
 * <p>Of course, you can, in fact, do anything you like with the {@code ResultSet}
 * acquired through a {@code CustomReader}, including closing it. <i>Klojang JDBC</i> does
 * not protect itself against such unintended usage and you will most likely trigger an
 * exception if you keep making <i>Klojang JDBC</i> calls afterward.
 * 
 * @see SessionConfig#getCustomReader(Class, String, Class, int)
 */
@FunctionalInterface
public interface CustomReader {
  /**
   * Retrieves the value of the designated column in the current row of the specified
   * {@link ResultSet}.
   *
   * @param resultSet the {@code ResultSet}
   * @param columnIndex the first column is 1, the second is 2, ...
   * @return the column value
   * @throws SQLException if the columnIndex is not valid or if a database access
   *       error occurs
   */
  Object getValue(ResultSet resultSet, int columnIndex) throws SQLException;
}
