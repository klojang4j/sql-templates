package org.klojang.jdbc;

import java.sql.PreparedStatement;
import java.util.function.BiConsumer;

/**
 * {@code BindInfo} objects allow you to override how values should be bound into a
 * {@link PreparedStatement}. Ordinarily <i>Klojang JDBC</i> will have enough context to
 * figure this out without intervention from the user. However, if you want or need to,
 * this class enables you to intervene anyhow.
 *
 * @author Ayco Holleman
 */
public interface BindInfo {

  /**
   * Allows you to specify the storage type for a property. The return value must be
   * {@code null} or one of the constants in {@link java.sql.Types} (like
   * {@link java.sql.Types#VARCHAR VARCHAR}). {@code null} means you leave it to
   * <i>Klojang JDBC</i> to determine the mapping. The default implementation returns
   * {@code null}. You may ignore any argument that you don't need to determine the SQL
   * type. For example, in most cases just the type of the property will likely be enough
   * to determine the corresponding SQL type.
   *
   * @param beanType the class containing the property whose SQL type to determine
   * @param propertyName the name of the property whose SQL type to determine
   * @param propertyType the type of the property whose SQL type to determine
   * @return one of the class constants of the {@link java.sql.Types} class or
   *     {@code null}
   */
  default Integer getSqlType(
      Class<?> beanType,
      String propertyName,
      Class<?> propertyType) {
    return null;
  }

  /**
   * Whether to save enums as strings (by calling their {@code toString()} or as ints (by
   * calling their {@code ordinal()} method. The default implementation return
   * {@code false}, meaning that by default <i>Klojang JDBC</i> will attempt to save enums
   * as ints. You can ignore any argument that you don't need to determine the storage
   * type. To save all enums in your application are strings, simply return {@code true}
   * straight away.
   *
   * @param beanType the class containing the enum property
   * @param enumProperty the enum property for which to specify the storage type.
   * @return whether to bind enums as strings or as ints
   */
  default boolean saveEnumAsString(Class<?> beanType, String enumProperty) {
    return false;
  }

}
