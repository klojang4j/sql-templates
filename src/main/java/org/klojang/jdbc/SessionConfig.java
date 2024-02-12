package org.klojang.jdbc;

import org.klojang.jdbc.x.Utils;
import org.klojang.templates.NameMapper;
import org.klojang.templates.name.CamelCaseToSnakeUpperCase;
import org.klojang.templates.name.SnakeCaseToCamelCase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static org.klojang.templates.name.CamelCaseToSnakeUpperCase.camelCaseToSnakeUpperCase;
import static org.klojang.templates.name.SnakeCaseToCamelCase.snakeCaseToCamelCase;

/**
 * {@code SessionConfig} objects allow you to fine-tune or modify <i>Klojang JDBC's</i>
 * default behaviour. You might want to implement {@code SessionConfig} through an
 * anonymous class:
 *
 * <blockquote><pre>{@code
 * SessionConfig config = new SessionConfig() {
 *   public boolean saveEnumAsString(Class<?> beanType, String enumProperty Class<?> enumType) {
 *     return true;
 *   }
 * };
 * }</pre></blockquote>
 *
 * @author Ayco Holleman
 */
public interface SessionConfig {

  /**
   * Returns a {@code SessionConfig} instance which does not override of the defaults
   * provided by the {@code SessionConfig} instance.
   *
   * @return a {@code SessionConfig} instance which does not override of the defaults
   *       provided by the {@code SessionConfig} instance
   */
  static SessionConfig getDefaultConfig() { return Utils.DEFAULT_CONFIG; }

  /**
   * A {@code CustomBinder} gives you full control over how values are bound to a
   * {@link PreparedStatement}. It essentially just hands you the underlying
   * {@link PreparedStatement} and lets you do the binding yourself. Of course, since you
   * are now in control of the {@code PreparedStatement}, you can do anything you like
   * with it, including closing it. <i>Klojang JDBC</i> will not be resistant against such
   * behaviour. A {@code CustomBinder} can be used, for example, to apply last-minute
   * transformations to the value that is about to be bound, or to serialize it in a
   * bespoke way, or to map it to a non-standard SQL datatype. When binding {@code Map}
   * values using {@link SQLStatement#bind(Map)}, custom binders will only kick in for
   * non-{@code null} values, because Java's type erase feature prevents the type of the
   * values from being established beforehand. When binding values in a JavaBean or
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

  interface CustomReader {
    Object getValue(ResultSet resultSet, int paramIndex) throws SQLException;
  }

  /**
   * Allows you to specify a {@code CustomBinder} for a given Java type. The default
   * implementation returns {@code null}, meaning you leave it to <i>Klojang JDBC</i> to
   * bind values to the underlying {@link PreparedStatement}. You may ignore any argument
   * that you don't need in order to determine whether to use a {@code CustomBinder}.
   *
   * @param beanType the type of the JavaBean, {@code record}, or {@code Map}
   *       containing the values for which to specify a {@code CustomBinder}
   * @param propertyName the name of the bean property, record component, or map key
   *       for which to specify a {@code CustomBinder}
   * @param propertyType the type of the values  for which to specify a
   *       {@code CustomBinder}
   * @return a {@code CustomBinder} for any combination of the provided arguments
   */
  default CustomBinder getCustomBinder(Class<?> beanType,
        String propertyName,
        Class<?> propertyType) {
    return null;
  }

  /**
   * Allows you to specify a {@code CustomReader} for a given Java type. The default
   * implementation returns {@code null}, meaning you leave it to <i>Klojang JDBC</i> to
   * extract values to the underlying {@link ResultSet}. You may ignore any argument that
   * you don't need in order to determine whether to use a {@code CustomReader}.
   *
   * @param beanType the type of the JavaBean, {@code record}, or {@code Map} that
   *       will receive the value from the {@code ResultSet}
   * @param propertyName the name of the bean property, record component, or map key
   *       that will receive the value from the {@code ResultSet}
   * @param propertyType the type of the values for which to specify a
   *       {@code CustomReader}
   * @param sqlType the SQL datatype of the column for which to specify a
   *       {@code CustomReader}. Must be one of the constants of the
   *       {@link java.sql.Types java.sql.Types}, like
   *       {@link java.sql.Types#VARCHAR Types.VARCHAR} or
   *       {@link java.sql.Types#TIMESTAMP Types.TIMESTAMP}}
   * @return a {@code CustomReader} for any combination of the provided arguments
   */
  default CustomReader getCustomReader(Class<?> beanType,
        String propertyName,
        Class<?> propertyType,
        int sqlType) {
    return null;
  }

  /**
   * Specifies the storage type (the SQL datatype) for a value. This method will only be
   * evaluated if {@link #getCustomBinder(Class, String, Class) getCustomBinder()}
   * returned {@code null}. The return value must either be one of the constants in the
   * {@link java.sql.Types java.sql.Types} class (like
   * {@link java.sql.Types#VARCHAR Types.VARCHAR}) or {@code null}. Returning {@code null}
   * means you leave it to <i>Klojang JDBC</i> to figure out the SQL datatype. The default
   * implementation returns {@code null}. You may ignore any argument that you don't need
   * in order to determine the SQL datatype. For example, in many cases the type of the
   * value is all you need to know in order to determine the corresponding SQL datatype.
   *
   * @param beanType the type of the JavaBean, {@code record}, or {@code Map}
   *       containing the value
   * @param propertyName the name of the bean property, record component, or map key
   *       for which to specify the SQL datatype.
   * @param propertyType the type of the value whose SQL datatype to determine
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
   * ints (by calling their {@code ordinal()} method). This method will only be evaluated
   * if {@link #getSqlType(Class, String, Class) getSqlType()} returned {@code null}. The
   * default implementation return {@code false}, meaning that by default <i>Klojang
   * JDBC</i> will save enums as ints. More precisely: <i>Klojang JDBC</i> will bind
   * {@code enum} types using {@code preparedStatement.setInt(myEnum.ordinal())} (thus the
   * target column may still be a VARCHAR column). Whichever option you choose, the
   * reverse process &#8212; copying {@code ResultSet} values to {@code enum} properties
   * &#8212; will always work correctly, without requiring extra configuration. You may
   * ignore any argument that you don't need in order to determine the storage type. To
   * save <i>all</i> enums in your application as strings, ignore all arguments and simply
   * return {@code true} straight away.
   *
   * @param beanType the type of the JavaBean, {@code record}, or {@code Map}
   *       containing the value
   * @param propertyName the name of the bean property, record component, or map key
   *       for which to specify the SQL datatype.
   * @param enumType the type of the {@code enum} value whose SQL datatype to
   *       determine
   * @return whether to bind enums as strings ({@code true}) or as ints ({@code false})
   */
  default boolean saveEnumAsString(Class<?> beanType,
        String propertyName,
        Class<? extends Enum<?>> enumType) {
    return false;
  }

  /**
   * Specifies the {@link NameMapper} to be used for mapping bean properties, record
   * components, or map keys to column names. The default implementation returns an
   * instance of {@link CamelCaseToSnakeUpperCase}.
   *
   * @return the {@link NameMapper} to be used for mapping bean properties, record
   *       components, or map keys to column names
   */
  default NameMapper getPropertyToColumnMapper() {
    return camelCaseToSnakeUpperCase();
  }

  /**
   * Specifies the {@link NameMapper} to be used for mapping column names to bean
   * properties, record components, or map keys. The default implementation returns an
   * instance of {@link SnakeCaseToCamelCase}.
   *
   * @return the {@link NameMapper} to be used for mapping column names to bean
   *       properties, record components, or map keys
   */
  default NameMapper getColumnToPropertyMapper() {
    return snakeCaseToCamelCase();
  }

  /**
   * Returns a new instance that is equal to this instance except with the
   * property-to-column mapper set to the specified {@code NameMapper}.
   *
   * @param mapper the {@code NameMapper} to be used for mapping bean properties,
   *       record components, or map keys to column names
   * @return a new instance that is equal to this instance except with the
   *       property-to-column mapper set to the specified {@code NameMapper}.
   */
  default SessionConfig withPropertyToColumnMapper(NameMapper mapper) {
    return new SessionConfig() {
      public NameMapper getPropertyToColumnMapper() { return mapper; }
    };
  }

  /**
   * Returns a new instance that is equal to this instance except with the
   * column-to-property mapper set to the specified {@code NameMapper}.
   *
   * @param mapper the {@code NameMapper} to be used for mapping column names to
   *       bean properties, record components, or map keys
   * @return a new instance that is equal to this instance except with the
   *       column-to-property mapper set to the specified {@code NameMapper}.
   */
  default SessionConfig withColumnToPropertyMapper(NameMapper mapper) {
    return new SessionConfig() {
      public NameMapper getColumnToPropertyMapper() { return mapper; }
    };
  }

  /**
   * Returns a new instance that is equal to this instance except that <i>all</i>
   * {@code enum} types will be persisted by calling {@code toString()} them.
   *
   * @return a new instance that is equal to this instance except that <i>all</i>
   *       {@code enum} types will be persisted by calling {@code toString()} them.
   */
  default SessionConfig withSaveAllEnumsAsStrings() {
    return new SessionConfig() {
      public boolean saveEnumAsString(Class<?> beanType,
            String propertyName,
            Class<? extends Enum<?>> enumType) {
        return true;
      }
    };
  }

}
