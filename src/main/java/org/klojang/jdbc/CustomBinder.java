package org.klojang.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 * <p>A {@code CustomBinder} gives you full control over how a value is bound to a
 * {@link PreparedStatement}. It simply hands you the {@link PreparedStatement} and lets
 * you do the binding yourself. A {@code CustomBinder} can be used, for example, to apply
 * last-minute transformations to the value, or to serialize it in a bespoke way, or to
 * map it to a non-standard SQL datatype. However, if you find yourself
 * {@linkplain SessionConfig#getCustomBinder(Class, String, Class) defining} a lot of
 * custom binders, <i>Klojang JDBC</i> might not be the right tool for you. When binding
 * {@code Map} values using {@link SQLStatement#bind(Map)}, custom binders will only kick
 * in for non-{@code null} values. When binding values in a JavaBean or {@code record},
 * custom binders will kick in even for {@code null} values.
 *
 * <p>Of course, you can, in fact, do anything you like with the {@code PreparedStatement}
 * acquired through a {@code CustomBinder}, including closing it. <i>Klojang JDBC</i> does
 * not protect itself against such unintended usage and you will most likely trigger an
 * exception if you keep making <i>Klojang JDBC</i> calls afterward.
 * 
 * @see SessionConfig#getCustomBinder(Class, String, Class)
 */
@FunctionalInterface
public interface CustomBinder {
  /**
   * Sets the value of the designated parameter using the given value.
   *
   * @param preparedStatement the {@code PreparedStatement} to bind the value to
   * @param paramIndex the first parameter is 1, the second is 2, ...
   * @param value the value to be bound
   * @throws SQLException if parameterIndex does not correspond to a parameter
   *       marker in the SQL statement, or if a database access error occurs
   */
  void bind(PreparedStatement preparedStatement, int paramIndex, Object value)
        throws SQLException;
}
