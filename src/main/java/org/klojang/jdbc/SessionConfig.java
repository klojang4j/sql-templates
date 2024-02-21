package org.klojang.jdbc;

import org.klojang.jdbc.x.Utils;
import org.klojang.templates.NameMapper;
import org.klojang.templates.name.CamelCaseToSnakeLowerCase;
import org.klojang.templates.name.CamelCaseToSnakeUpperCase;
import org.klojang.templates.name.SnakeCaseToCamelCase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Map;
import java.util.function.Function;

import static org.klojang.templates.name.CamelCaseToSnakeLowerCase.camelCaseToSnakeLowerCase;
import static org.klojang.templates.name.CamelCaseToSnakeUpperCase.camelCaseToSnakeUpperCase;
import static org.klojang.templates.name.SnakeCaseToCamelCase.snakeCaseToCamelCase;

/**
 * {@code SessionConfig} objects allow you to fine-tune or modify various aspects of how
 * <i>Klojang JDBC</i> processes and executes SQL. {@code SessionConfig} is an interface
 * that provides default implementations for all its methods. The default implementations
 * specify <i>Klojang JDBC's</i> default behaviour. You might want to implement
 * {@code SessionConfig} through an anonymous class:
 *
 * <blockquote><pre>{@code
 * SessionConfig config = new SessionConfig() {
 *   public boolean saveEnumAsString(Class<?> beanType, String enumProperty Class<?> enumType) {
 *     return true;
 *   }
 * };
 * }</pre></blockquote>
 *
 * <p>Alternatively, you could use the provided "withers" to modify how <i>Klojang
 * JDBC</i> operates:
 *
 * <blockquote><pre>{@code
 * SessionConfig config = SessionConfig.getDefaultConfig().withEnumsSavedAsStrings();
 * }</pre></blockquote>
 *
 * @author Ayco Holleman
 */
public interface SessionConfig {

  /**
   * Returns a {@code SessionConfig} instance which does not override any of the defaults
   * provided by the {@code SessionConfig} instance.
   *
   * @return a {@code SessionConfig} instance which does not override any of the defaults
   *       provided by the {@code SessionConfig} instance
   */
  static SessionConfig getDefaultConfig() { return Utils.DEFAULT_CONFIG; }

  /**
   * A {@code CustomBinder} gives you full control over how a value is bound to a
   * {@link PreparedStatement}. It hands you the underlying {@link PreparedStatement} and
   * lets you do the binding yourself. Of course, since you are now in control of the
   * {@code PreparedStatement}, you can do anything you like with it, including closing
   * it. <i>Klojang JDBC</i> will not be resistant against such behaviour. A
   * {@code CustomBinder} can be used, for example, to apply last-minute transformations
   * to the value, or to serialize it in a bespoke way, or to map it to a non-standard SQL
   * datatype. When binding {@code Map} values using {@link SQLStatement#bind(Map)},
   * custom binders will only kick in for non-{@code null} values. When binding values in
   * a JavaBean or {@code record}, custom binders will kick in even for {@code null}
   * values.
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
    void bind(PreparedStatement preparedStatement, int paramIndex, Object value)
          throws SQLException;
  }

  /**
   * A {@code CustomReader} gives you full control over how a value is extracted from a
   * {@link ResultSet}. It hands you the underlying {@code ResultSet} and lets you extract
   * the value yourself. If the value is to be assigned to a JavaBean property or
   * {@code record} component, it is your responsibility to ensure the value is
   * type-compatible with the property or component, or a {@link ClassCastException} will
   * ensue.
   */
  @FunctionalInterface
  interface CustomReader {
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

  /**
   * Returns a {@code CustomBinder} for the provided combination of arguments, or
   * {@code null} if no {@code CustomBinder} is required for the provided combination of
   * arguments. The default implementation returns {@code null}. You may ignore any or all
   * arguments that you don't need in order to determine whether to use a
   * {@code CustomBinder}.
   *
   * @param beanType the type of the JavaBean, {@code record}, or {@code Map}
   *       containing the values for which to specify a {@code CustomBinder}
   * @param propertyName the name of the bean property, record component, or map key
   *       for which to specify a {@code CustomBinder}
   * @param propertyType the type of the values for which to specify a
   *       {@code CustomBinder}
   * @return a {@code CustomBinder} for the provided combination of arguments
   */
  default CustomBinder getCustomBinder(Class<?> beanType,
        String propertyName,
        Class<?> propertyType) {
    return null;
  }

  /**
   * Returns a {@code CustomReader} for the provided combination of arguments, or
   * {@code null} if no {@code CustomReader} is required for the provided combination of
   * arguments. The default implementation returns {@code null}. You may ignore any or all
   * arguments that you don't need in order to determine whether to use a
   * {@code CustomReader}.
   *
   * @param beanType the type of the JavaBean, {@code record}, or {@code Map} that
   *       will receive the value from the {@code ResultSet}. Note that when writing to a
   *       {@code Map} (using a {@link MapExtractor}), this argument will always be
   *       {@code HashMap.class} because that happens to be the type of {@code Map} that a
   *       {@link MapExtractor} creates.
   * @param propertyName the name of the bean property, record component, or map key
   *       that will receive the value from the {@code ResultSet}
   * @param propertyType the type of the values for which to specify a
   *       {@code CustomReader}. Note that when writing to a {@code Map} (using a
   *       {@link MapExtractor}), this argument will always be {@code Object.class}
   *       because we don't know the runtime type yet of the values when the
   *       {@code MapExtractor} is configured, and Java's type erase feature prevents us
   *       from being any more specific beforehand.
   * @param sqlType the SQL datatype of the column for which to specify a
   *       {@code CustomReader}. Must be one of the constants of the
   *       {@link java.sql.Types java.sql.Types}, like
   *       {@link java.sql.Types#VARCHAR Types.VARCHAR} or
   *       {@link java.sql.Types#TIMESTAMP Types.TIMESTAMP}}
   * @return a {@code CustomReader} for the provided combination of arguments
   */
  default CustomReader getCustomReader(Class<?> beanType,
        String propertyName,
        Class<?> propertyType,
        int sqlType) {
    return null;
  }

  /**
   * Returns the SQL datatype for the provided combination of arguments, or {@code null}
   * if want you leave it to <i>Klojang JDBC</i> to determine the SQL datatype. The
   * default implementation returns {@code null}. If not {@code null}, the return value
   * must be one of the constants in the {@link java.sql.Types java.sql.Types} class (like
   * {@link java.sql.Types#VARCHAR Types.VARCHAR}). This method will only be evaluated if
   * {@link #getCustomBinder(Class, String, Class) getCustomBinder()} returned
   * {@code null}. You may ignore any or all arguments that you don't need in order to
   * determine the return value. For example, in many cases the type of the value
   * (provided via the {@code propertyType} argument) will be all you need to know in
   * order to determine the corresponding SQL datatype.
   *
   * @param beanType the type of the JavaBean, {@code record}, or {@code Map}
   *       containing the value
   * @param propertyName the name of the bean property, record component, or map key
   *       for which to specify the SQL datatype.
   * @param propertyType the type of the value whose SQL datatype to determine
   * @return one of the class constants of the {@link java.sql.Types java.sql.Types} class
   *       or {@code null}
   */
  default Integer getSQLType(Class<?> beanType,
        String propertyName,
        Class<?> propertyType) {
    return null;
  }

  /**
   * <p>Whether to save enums as strings (by calling their {@code toString()} method) or
   * as ints (by calling their {@code ordinal()} method). The default implementation
   * returns {@code false}, meaning that by default <i>Klojang JDBC</i> will save enums as
   * ints. More precisely: enums will be <i>bound</i> using
   * {@code preparedStatement.setInt(paramIndex, myEnum.ordinal())}. The target column
   * may
   * <i>still</i> be a {@code VARCHAR} column since most databases will easily convert
   * integers to strings when needed. Whichever option you choose, the reverse process
   * &#8212; converting {@code ResultSet} values to enums &#8212; will always work
   * correctly, without requiring additional configuration. You may ignore any or all
   * arguments that you don't need in order to determine the return value. To save
   * <i>all</i> enums in your application as strings, ignore all arguments and simply
   * return {@code true} straight away.
   *
   * <p>This method will only be evaluated if
   * {@link #getSQLType(Class, String, Class) getSqlType()} returned {@code null}.
   *
   * @param beanType the type of the JavaBean, {@code record}, or {@code Map}
   *       containing the value
   * @param enumProperty the name of the bean property, record component, or map key
   *       for which to specify the SQL datatype
   * @param enumType the type of the {@code enum} value whose SQL datatype to
   *       determine
   * @return whether to bind enums as strings ({@code true}) or as ints ({@code false})
   */
  default boolean saveEnumAsString(Class<?> beanType,
        String enumProperty,
        Class<? extends Enum<?>> enumType) {
    return false;
  }

  /**
   * Returns the {@code DateTimeFormatter} to be used for date/time values, or
   * {@code null} if the provided combination of arguments does not require the use of a
   * {@code DateTimeFormatter}. The default implementation returns {@code null}. If a
   * non-{@code null} value is returned, date/time objects will be saved as a string
   * rather than as a SQL {@link java.sql.Types#DATE} or {@link java.sql.Types#TIMESTAMP}.
   * More precisely: they will be <i>bound</i> using
   * {@code preparedStatement.setString(paramIndex, formatter.format(value))}. The target
   * column may <i>still</i> be a {@code DATE} or {@code TIMESTAMP} column since most
   * databases will easily convert a (properly formatted) string to a {@code DATE} or
   * {@code TIMESTAMP}. You may ignore any or all arguments that you don't need in order
   * to determine the return value.
   *
   * <p>This method will only be evaluated if
   * {@link #getSQLType(Class, String, Class) getSqlType()} returned {@code null}.
   *
   * @param beanType the type of the JavaBean, {@code record}, or {@code Map}
   *       containing the value
   * @param dateTimeProperty the name of the bean property, record component, or map
   *       key for which to use the {@code DateTimeFormatter}
   * @param dateTimeType the exact date/time type for which to use the
   *       {@code DateTimeFormatter}
   * @return the {@code DateTimeFormatter} to be used for a date/time values, or
   *       {@code null} if the provided combination of arguments does not require the use
   *       of a {@code DateTimeFormatter}.
   */
  default DateTimeFormatter getDateTimeFormatter(Class<?> beanType,
        String dateTimeProperty,
        Class<? extends TemporalAccessor> dateTimeType) {
    return null;
  }

  /**
   * Returns the serialization function to be used for the provided combination of
   * arguments, or {@code null} if no special serialization is required for the provided
   * combination of arguments. The default implementation returns {@code null}. If a
   * non-{@code null} value is returned, objects will be bound using
   * {@code preparedStatement.setString(paramIndex, function.apply(object))}. You may
   * ignore any or all arguments that you don't need in order to determine the return
   * value.
   *
   * <p>This method can be used to save complex types for which no default java-to-SQL
   * type mapping exists. In that case, it pays if the type contains a static factory
   * method that takes a {@code String} and returns an instance of that type. <i>Klojang
   * JDBC</i> will detect the factory method and use it for the reverse process:
   * deserializing {@link ResultSet} values into instances of that type. Alternatively, if
   * the type contains a constructor that takes a single {@code String} argument, then
   * that constructor will be used as the deserialization mechanism. Otherwise specify a
   * {@link #getCustomReader(Class, String, Class, int) CustomReader} that will
   * deserialize the values.
   *
   * @param beanType the type of the JavaBean, {@code record}, or {@code Map}
   *       containing the value to be bound
   * @param propertyName the name of the bean property, record component, or map key
   *       for which to specify the serializer function
   * @param propertyType the type of the value for which to specify the serializer
   *       function
   * @return the serialization function to be used for the provided combination of
   *       arguments, or {@code null} if no special serialization is required for the
   *       provided combination of arguments
   */
  default Function<Object, String> getSerializer(Class<?> beanType,
        String propertyName,
        Class<?> propertyType) {
    return null;
  }

  /**
   * Returns the serialization function to be used for the provided combination of
   * arguments, or {@code null} if no special serialization is required for the provided
   * combination of arguments. The default implementation returns {@code null}. If a
   * non-{@code null} value is returned, objects will be bound using
   * {@code preparedStatement.setBytes(paramIndex, function.apply(object))}. You may
   * ignore any or all arguments that you don't need in order to determine the return
   * value.
   *
   * <p>This method can be used to save complex types for which no default java-to-SQL
   * type mapping exists. In that case, it pays if the type contains a static factory
   * method that takes a {@code byte[]} array and returns an instance of that type.
   * <i>Klojang JDBC</i> will detect the factory method and use it for the reverse
   * process: deserializing {@link ResultSet} values into instances of that type.
   * Alternatively, if the type contains a constructor that takes a single {@code byte[]}
   * argument, then that constructor will be used as the deserialization mechanism.
   * Otherwise specify a {@link #getCustomReader(Class, String, Class, int) CustomReader}
   * that will deserialize the values.
   *
   * @param beanType the type of the JavaBean, {@code record}, or {@code Map}
   *       containing the value to be bound
   * @param propertyName the name of the bean property, record component, or map key
   *       for which to specify the serializer function
   * @param propertyType the type of the value for which to specify the serializer
   *       function
   * @return the serialization function to be used for the provided combination of
   *       arguments, or {@code null} if no special serialization is required for the
   *       provided combination of arguments
   */
  default Function<Object, byte[]> getBinarySerializer(Class<?> beanType,
        String propertyName,
        Class<?> propertyType) {
    return null;
  }

  /**
   * Specifies the {@link NameMapper} to be used for mapping bean properties, record
   * components, or map keys to column names. The default implementation returns
   * {@link CamelCaseToSnakeUpperCase#camelCaseToSnakeUpperCase()
   * camelCaseToSnakeUpperCase()}, which would map {@code "camelCaseToSnakeUpperCase"} to
   * {@code "CAMEL_CASE_TO_SNAKE_UPPER_CASE"}. (It would also map {@code "WordCase"}
   * a.k.a. {@code "PascalCase"} to {@code "WORD_CASE"} and {@code "PASCAL_CASE"},
   * respectively, since all characters end up in upper case anyhow.)
   *
   * @return the {@link NameMapper} to be used for mapping bean properties, record
   *       components, or map keys to column names
   */
  default NameMapper getPropertyToColumnMapper() {
    return camelCaseToSnakeUpperCase();
  }

  /**
   * Specifies the {@link NameMapper} to be used for mapping column names to bean
   * properties, record components, or map keys. The default implementation returns
   * {@link SnakeCaseToCamelCase#snakeCaseToCamelCase() snakeCaseToCamelCase()}, which
   * would map {@code "SNAKE_CASE_TO_CAMEL_CASE}" to {@code "snakeCaseToCamelCase"}. (It
   * would also map {@code "snake_case_to_camel_case"} to
   * {@code "SNAKE_CASE_TO_CAMEL_CASE}" because the casing of the input string is
   * irrelevant for this {@code NameMapper}.)
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
   * Returns a new instance that is equal to this instance except that property names are
   * mapped <i>as-is</i> to column names and vice versa.
   *
   * @return a new instance that is equal to this instance except that property names are
   *       mapped <i>as-is</i> to column names and vice versa
   * @see NameMapper#AS_IS
   */
  default SessionConfig withNameMappingDisabled() {
    return new SessionConfig() {
      public NameMapper getPropertyToColumnMapper() { return NameMapper.AS_IS; }

      public NameMapper getColumnToPropertyMapper() { return NameMapper.AS_IS; }
    };
  }

  /**
   * Returns a new instance that is equal to this instance except that property names are
   * mapped to column names using the
   * {@link CamelCaseToSnakeLowerCase#camelCaseToSnakeLowerCase()
   * camelCaseToSnakeLowerCase()} name mapper. This {@code NameMapper} would map
   * {@code "camelCaseToSnakeLowerCase"} to {@code "camel_case_to_snake_lower_case"}. It
   * would also map {@code "WordCase"} a.k.a. {@code "PascalCase"} to {@code "word_case"}
   * and {@code "pascal_case"}, respectively, since all characters end up in lower case
   * anyhow. The reverse (column-to-property) mapper remains at its default value
   * ({@link SnakeCaseToCamelCase#snakeCaseToCamelCase() snakeCaseToCamelCase()}). When
   * mapping snake case names to camel case names, the casing of the input string is
   * irrelevant.
   *
   * @return a new instance that is equal to this instance except that property names are
   *       mapped to column names using the
   *       {@link CamelCaseToSnakeLowerCase#camelCaseToSnakeLowerCase()
   *       camelCaseToSnakeLowerCase()} name mapper
   */
  default SessionConfig withLowerCaseColumnNames() {
    return new SessionConfig() {
      public NameMapper getPropertyToColumnMapper() { return camelCaseToSnakeLowerCase(); }
    };
  }

  /**
   * Returns a new instance that is equal to this instance except that <i>all</i> enums
   * will be saved by calling {@code toString()} on them.
   *
   * @return a new instance that is equal to this instance except that <i>all</i> enums
   *       will be saved by calling {@code toString()} on them.
   */
  default SessionConfig withEnumsSavedAsStrings() {
    return new SessionConfig() {
      public boolean saveEnumAsString(Class<?> beanType,
            String enumProperty,
            Class<? extends Enum<?>> enumType) {
        return true;
      }
    };
  }

}
