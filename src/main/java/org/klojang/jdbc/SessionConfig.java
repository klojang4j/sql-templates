package org.klojang.jdbc;

import org.klojang.jdbc.x.Utils;
import org.klojang.templates.NameMapper;
import org.klojang.templates.name.CamelCaseToSnakeLowerCase;
import org.klojang.templates.name.CamelCaseToSnakeUpperCase;
import org.klojang.templates.name.SnakeCaseToCamelCase;

import java.sql.ResultSet;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.function.Function;

import static org.klojang.templates.name.CamelCaseToSnakeLowerCase.camelCaseToSnakeLowerCase;
import static org.klojang.templates.name.CamelCaseToSnakeUpperCase.camelCaseToSnakeUpperCase;
import static org.klojang.templates.name.SnakeCaseToCamelCase.snakeCaseToCamelCase;

/**
 * <p>{@code SessionConfig} objects allow you to fine-tune or modify various aspects of
 * how
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
 * <p>Whichever option you choose, assuming you will need just one, or maybe two
 * {@code org.klojang.templates.SessionConfig SessionConfig} objects in your entire
 * application, it is recommended that you store them in {@code public static final}
 * fields, and share them whenever and wherever possible.
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
   * <p>Returns the serialization function to be used for the provided combination of
   * arguments, or {@code null} if no special serialization is required for the provided
   * combination of arguments. The default implementation returns {@code null}. If a
   * non-{@code null} value is returned, objects will be bound using
   * {@code preparedStatement.setString(paramIndex, function.apply(object))}. You may
   * ignore any or all arguments that you don't need in order to determine the return
   * value.
   *
   * <p>This method can be used to save types for which no default java-to-SQL
   * type mapping exists. If you specify a serializer for such a type, it pays if the type
   * also contains a static factory method that takes a {@code String} and returns an
   * instance of that type (in other words, a deserializer). If there is exactly one such
   * method, <i>Klojang JDBC</i> will use it for the reverse process: deserializing
   * {@link ResultSet} values into instances of that type. Alternatively, if the type
   * contains a constructor that takes a single {@code String} argument, then that
   * constructor will be used as the deserialization mechanism. This works for classes
   * like {@link StringBuilder}, but it may be assuming a bit too much for other classes.
   * In that case, specify a
   * {@link #getCustomReader(Class, String, Class, int) CustomReader} that will
   * deserialize the values.
   *
   * <p>Note that if the type is properly serialized through its {@code toString()}
   * method, you don't need to specify a serializer for it. <i>Klojang JDBC</i> will
   * already, as a last resort, call {@code toString()} on (non-{@code null}) values with
   * an unknown type. More precisely, it will bind them using
   * {@code preparedStatement.setString(paramIndex, value.toString())}.
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
   * <p>Returns the serialization function to be used for the provided combination of
   * arguments, or {@code null} if no special serialization is required for the provided
   * combination of arguments. The default implementation returns {@code null}. If a
   * non-{@code null} value is returned, objects will be bound using
   * {@code preparedStatement.setBytes(paramIndex, function.apply(object))}. You may
   * ignore any or all arguments that you don't need in order to determine the return
   * value.
   *
   * <p>This method can be used to save types for which no default java-to-SQL
   * type mapping exists. If you specify a serializer for such a type, it pays if the type
   * also contains a static factory method that takes a {@code String} and returns an
   * instance of that type (in other words, a deserializer). If there is exactly one such
   * method, <i>Klojang JDBC</i> will use it for the reverse process: deserializing
   * {@link ResultSet} values into instances of that type. Alternatively, if the type
   * contains a constructor that takes a single {@code byte[]} array argument, then that
   * constructor will be used as the deserialization mechanism. That may work for some
   * classes, but it may be assuming a bit too much for other classes. In that case,
   * specify a {@link #getCustomReader(Class, String, Class, int) CustomReader} that will
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
  default SessionConfig withNamesMappedAsIs() {
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
