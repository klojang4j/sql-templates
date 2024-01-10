package org.klojang.jdbc;

import java.sql.PreparedStatement;

/**
 * {@code BindInfo} objects allow you to determine how values are bound into a
 * {@link PreparedStatement}. Ordinarily <i>Klojang JDBC</i> will have enough context to
 * figure this out automatically. However, if you want or need to, this class enables you
 * to override the default behaviour.
 *
 * @author Ayco Holleman
 */
public interface BindInfo {

  /**
   * Allows you to specify the storage type for a bean property or record component. The
   * return value must either be one of the constants in the
   * {@link java.sql.Types java.sql.Types} class (like
   * {@link java.sql.Types#VARCHAR VARCHAR}) or {@code null}. Returning {@code null} means
   * you leave it to <i>Klojang JDBC</i> to determine the storage type. The default
   * implementation returns {@code null}. You may ignore any argument that you don't need
   * in order to determine the storage type. For example, in many cases the type of the
   * property will be enough to determine the corresponding SQL type; you don't need to
   * know the type of the bean containing the property.
   *
   * @param beanType the class containing the property whose SQL type to determine
   * @param propertyName the name of the property whose SQL type to determine
   * @param javaType the type of the property whose SQL type to determine
   * @return one of the class constants of the {@link java.sql.Types} class or
   *       {@code null}
   */
  default Integer getSqlType(
        Class<?> beanType,
        String propertyName,
        Class<?> javaType) {
    return null;
  }

  /**
   * Whether to save enums as strings (by calling their {@code toString()} method) or as
   * ints (by calling their {@code ordinal()} method). The default implementation return
   * {@code false}, meaning that by default <i>Klojang JDBC</i> will save enums as ints.
   * You can ignore any argument that you don't need in order to determine the storage
   * type. To save <i>all</i> enums in your application are strings, simply return
   * {@code true} straight away.
   *
   * @param beanType the class containing the enum property
   * @param enumProperty the enum property for which to specify the storage type.
   * @return whether to bind enums as strings or as ints
   */
  default boolean saveEnumAsString(Class<?> beanType, String enumProperty) {
    return false;
  }

}
