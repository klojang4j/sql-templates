package org.klojang.jdbc;

import java.sql.PreparedStatement;

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
   * A {@code BindInfo} that does not override the default binding behaviour.
   */
  BindInfo DEFAULT = new BindInfo() { };

  /**
   * Specifies the SQL datatype of the column corresponding to a bean property. The return
   * value must either be one of the constants in the
   * {@link java.sql.Types java.sql.Types} class (like
   * {@link java.sql.Types#VARCHAR Types.VARCHAR}) or {@code null}. Returning {@code null}
   * means you leave it to <i>Klojang JDBC</i> to figure out the SQL datatype. The default
   * implementation returns {@code null}. You may ignore any argument that you don't need
   * in order to determine the SQL datatype. For example, in many cases the type of the
   * property is all you need in order to determine the corresponding SQL datatype &#8212;
   * you don't need to know the name of the property or the type of the bean containing
   * the property.
   *
   * @param beanType the class containing the property whose SQL datatype to
   *       determine (may be a {@code record} type)
   * @param propertyName the name of the property or record component whose SQL
   *       datatype to determine
   * @param propertyType the type of the property whose SQL datatype to determine
   * @return one of the class constants of the {@link java.sql.Types java.sql.Types} class
   *       or {@code null}
   */
  default Integer getSqlType(Class<?> beanType,
        String propertyName,
        Class<?> propertyType) {
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
   * @param beanType the class containing the enum property (may be a {@code record}
   *       type)
   * @param enumProperty the enum property for which to specify the storage type.
   * @return whether to bind enums as strings ({@code true}) or as ints ({@code false})
   */
  default boolean saveEnumAsString(Class<?> beanType, String enumProperty) {
    return false;
  }

}
