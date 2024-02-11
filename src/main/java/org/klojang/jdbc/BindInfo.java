package org.klojang.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 * {@code BindInfo} objects allow you to determine how values are bound into a
 * {@link PreparedStatement}. Ordinarily <i>Klojang JDBC</i> will have enough context to
 * figure this out automatically. However, if you want or need to, this interface enables
 * you to override the default behaviour. You might want to implement {@code BindInfo}
 * through an anonymous class:
 *
 * <blockquote><pre>{@code
 * BindInfo bindInfo = new BindInfo() {
 *   public boolean saveEnumAsString(Class<?> beanType, String enumProperty) {
 *     return true;
 *   }
 * };
 * }</pre></blockquote>
 *
 * @author Ayco Holleman
 */
public interface BindInfo {

  /**
   * A {@code CustomBinder} gives you full control over how values are bound to a
   * {@link PreparedStatement}. It essentially just hands you the underlying
   * {@link PreparedStatement} so you can bind the values yourself. Of course, since you
   * are now in control of the {@code PreparedStatement}, you can do anything you like
   * with it, including closing it. <i>Klojang JDBC</i> will not be resistant against such
   * behaviour. A {@code CustomBinder} can be used, for example, to apply last-minute
   * transformations to the value that is about to be bound, or to serialize it in a
   * bespoke way, or to map it to a non-standard SQL datatype. When binding values in a
   * {@code Map} (using {@link SQLStatement#bind(Map)}), custom binders will only kick in
   * for non-{@code null} map values, because Java's type erase feature prevents the type
   * of the values from being established beforehand. When binding values in a JavaBean or
   * {@code record}, custom binders will kick in even for {@code null} values.
   */
  @FunctionalInterface
  interface CustomBinder {
    /**
     * Sets the value of the designated parameter using the given value.
     *
     * @param preparedStatement the {@code PreparedStatement} to bind the value to
     * @param paramIndex the first parameter is 1, the second is 2, ...
     * @param value the value to be bound
     * @throws SQLException if parameterIndex does not correspond to a parameter
     *       marker in the SQL statement, or if a database access error occurs
     */
    void bind(PreparedStatement preparedStatement, int paramIndex, Object value) throws
          SQLException;
  }

  /**
   * A {@code BindInfo} object which does not override the default binding behaviour.
   */
  BindInfo DEFAULT = new BindInfo() { };

  /**
   * Allows you to specify a {@code CustomBinder} for a given Java type. The default
   * implementation returns {@code null}, meaning you leave it to <i>Klojang JDBC</i> to
   * bind values to the underlying {@link PreparedStatement}, whatever the type of the
   * values, whatever the property, record component or map key that the values are
   * associated with, and whatever the bean, {@code record}, or {@code Map} containing the
   * values. You may ignore any argument that you don't need in order to determine whether
   * to use a {@code ManualBinder}.
   *
   * @param javaType the type of the values for which you want to do the binding
   *       yourself
   * @param containerType the class containing the values. May be a JavaBean type, a
   *       {@code record} type, or the type of the {@code Map} being bound using
   *       {@link SQLStatement#bind(Map)}. In the latter case, it will always be the
   *       concrete type of the {@code Map} (e.g. {@code HashMap.class}) &#8212; never
   *       {@code Map.class} itself.
   * @param name the name of the bean property, record component, or map key for
   *       which you want to do the binding yourself
   * @return a {@code CustomBinder} for any or all of the provided arguments
   */
  default CustomBinder getCustomBinder(Class<?> javaType,
        Class<?> containerType,
        String name) {
    return null;
  }

  /**
   * Specifies the storage type (the SQL datatype) for a value. The return value must
   * either be one of the constants in the {@link java.sql.Types java.sql.Types} class
   * (like {@link java.sql.Types#VARCHAR Types.VARCHAR}) or {@code null}. Returning
   * {@code null} means you leave it to <i>Klojang JDBC</i> to figure out the SQL
   * datatype. The default implementation returns {@code null}. You may ignore any
   * argument that you don't need in order to determine the SQL datatype. For example, in
   * many cases the type of the value is all you need to know in order to determine the
   * corresponding SQL datatype.
   *
   * @param javaType the type of the value whose SQL datatype to determine
   * @param containerType the class containing the value. May be a JavaBean type, a
   *       {@code record} type, or the type of the {@code Map} being bound using
   *       {@link SQLStatement#bind(Map)}. In the latter case, it will always be the
   *       concrete type of the {@code Map} (e.g. {@code HashMap.class}) &#8212; never
   *       {@code Map.class} itself.
   * @param name the name of the bean property, record component, or map key for
   *       which to specify the SQL datatype.
   * @return one of the class constants of the {@link java.sql.Types java.sql.Types} class
   *       or {@code null}
   */
  default Integer getSqlType(Class<?> javaType, Class<?> containerType, String name) {
    return null;
  }

  /**
   * Whether to save enums as strings (by calling their {@code toString()} method) or as
   * ints (by calling their {@code ordinal()} method). The default implementation return
   * {@code false}, meaning that by default <i>Klojang JDBC</i> will save enums as ints.
   * More precisely: <i>Klojang JDBC</i> will bind {@code enum} types using
   * {@code preparedStatement.setInt(myEnum.ordinal())}. You can ignore any argument that
   * you don't need in order to determine the storage type. To save <i>all</i> enums in
   * your application as strings, simply return {@code true} straight away.
   *
   * @param containerType the class containing the enum value. May be a JavaBean
   *       type, a {@code record} type, or the type of the {@code Map} being bound using
   *       {@link SQLStatement#bind(Map)}. In the latter case, it will always be the
   *       concrete type of the {@code Map} (e.g. {@code HashMap.class}) &#8212; never
   *       {@code Map.class} itself.
   * @param name the name of the bean property, record component, or map key for
   *       which to specify the SQL datatype.
   * @return whether to bind enums as strings ({@code true}) or as ints ({@code false})
   */
  default boolean saveEnumAsString(Class<?> containerType, String name) {
    return false;
  }

}
