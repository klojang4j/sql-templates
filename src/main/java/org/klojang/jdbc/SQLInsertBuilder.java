package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.check.ObjectCheck;
import org.klojang.invoke.Getter;
import org.klojang.invoke.GetterFactory;
import org.klojang.templates.NameMapper;

import java.sql.Connection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.joining;
import static org.klojang.check.CommonChecks.*;
import static org.klojang.check.CommonExceptions.STATE;
import static org.klojang.check.CommonExceptions.illegalState;
import static org.klojang.check.Tag.PROPERTIES;
import static org.klojang.util.ObjectMethods.ifNull;
import static org.klojang.util.ObjectMethods.isEmpty;
import static org.klojang.util.StringMethods.append;

/**
 * A {@code Builder} class for {@link SQLInsert} instances. This is the only class in
 * <i>Klojang JDBC</i> that deviates from its design decision to <i>not</i> provide
 * fluent APIs to mimic SQL statements (since we already have SQL for that). In this
 * particular case, however, it provides just to must convenience for the user to let
 * slip.
 */
public final class SQLInsertBuilder {

  static final String PROPERTIES_ALREADY_SET = "properties to include/exclude can only be set once";

  private Class<?> beanClass;
  private String tableName;
  private String[] properties;
  private boolean exclude;

  private NameMapper mapper = NameMapper.AS_IS;
  private BindInfo bindInfo = new BindInfo() {};

  SQLInsertBuilder() {}

  /**
   * Sets the type of the beans to be persisted.
   *
   * @param beanClass the type of the beans to be persisted
   * @return this {@code SQLInsertBuilder}
   */
  public SQLInsertBuilder of(Class<?> beanClass) {
    this.beanClass = Check.notNull(beanClass).ok();
    return this;
  }

  /**
   * Sets the table name to insert the data into. If not specified, this
   *
   * @param tableName the table name to insert the data into
   * @return this {@code SQLInsertBuilder}
   */
  public SQLInsertBuilder into(String tableName) {
    this.tableName = Check.that(tableName).isNot(empty()).ok();
    return this;
  }

  /**
   * Sets the properties and (corresponding columns) to exclude from the INSERT
   * statement. You would most likely at least want to exclude the property
   * corresponding to the auto-generated key column. It makes no sense to call both this
   * method and the {@link #including(String...)} on the same
   * {@code SQLBatchInsertBuilder} instance. The last call will override the effect of
   * any previous calls to {@code including()} and {@code excluding()}.
   *
   * @param properties the properties and (corresponding columns) to exclude from the
   * INSERT statement
   * @return this {@code SQLInsertBuilder}
   */
  public SQLInsertBuilder excluding(String... properties) {
    Check.that(this.properties).is(NULL(), illegalState(PROPERTIES_ALREADY_SET));
    this.properties = Check.notNull(properties, PROPERTIES).ok();
    this.exclude = true;
    return this;
  }

  /**
   * Sets the properties and (corresponding columns) to include in the INSERT statement.
   * It makes no sense to call both this method and the {@link #excluding(String...)} on
   * the same {@code SQLInsertBuilder} instance. The last call will override the effect
   * of any previous calls to {@code including()} and {@code excluding()}.
   *
   * @param properties the properties and (corresponding columns) to include in the
   * INSERT statement
   * @return this {@code SQLInsertBuilder}
   */
  public SQLInsertBuilder including(String... properties) {
    Check.that(this.properties).is(NULL(), illegalState(PROPERTIES_ALREADY_SET));
    this.properties = Check.notNull(properties, PROPERTIES).ok();
    this.exclude = false;
    return this;
  }

  /**
   * Sets the property-to-column mapper to be used when mapping bean properties to
   * column names. Beware of the direction of the mappings: <i>from</i> bean properties
   * <i>to</i> column names.
   *
   * @param propertyToColumnMapper the property-to-column mapper
   * @return this {@code SQLInsertBuilder}
   */
  public SQLInsertBuilder withMapper(NameMapper propertyToColumnMapper) {
    this.mapper = Check.notNull(propertyToColumnMapper).ok();
    return this;
  }

  /**
   * Sets the {@link BindInfo} object to be used to fine-tune the binding process
   *
   * @param bindInfo the {@code BindInfo} object to be used to fine-tune the binding
   * process
   * @return this {@code SQLInsertBuilder}
   */
  public SQLInsertBuilder withBindInfo(BindInfo bindInfo) {
    this.bindInfo = Check.notNull(bindInfo).ok();
    return this;
  }

  public SQLInsert prepare(Connection con) {
    return prepare(con, true);
  }

  /**
   * Creates and returns a {@code SQLInsert} instance using the input provided via the
   * other methods
   *
   * @param con the JDBC {@code Connection} to use for the INSERT statement
   * @return a {@code SQLInsert} instance
   */
  public SQLInsert prepare(Connection con, boolean retrieveAutoKeys) {
    Check.notNull(con);
    Check.on(STATE, beanClass, "beanClass").is(notNull());
    Map<String, Getter> getters = GetterFactory.INSTANCE.getGetters(beanClass, true);
    Set<String> props = getters.keySet();
    if (!isEmpty(properties)) {
      if (exclude) {
        props = new LinkedHashSet<>(props);
        for (String prop : properties) {
          checkProperty(props, prop).then(props::remove);
        }
      } else {
        Set<String> tmp = new LinkedHashSet<>(properties.length);
        for (String prop : properties) {
          checkProperty(props, prop).then(tmp::add);
        }
        props = tmp;
      }
    }
    String cols = props.stream().map(mapper::map).collect(joining(","));
    String params = props.stream().map(s -> ":" + s).collect(joining(","));
    String table = ifNull(tableName, beanClass.getSimpleName());
    StringBuilder sb = new StringBuilder(100);
    append(sb, "INSERT INTO ", table, " (", cols, ") VALUES(", params, ")");
    SQLSession sql = SQL.basic(sb.toString(), bindInfo).session(con);
    return sql.prepareInsert(retrieveAutoKeys);
  }

  private ObjectCheck<String, IllegalStateException> checkProperty(Set<String> props,
        String prop) {
    return Check.on(STATE, prop)
          .isNot(empty(), "empty property name not allowed")
          .is(in(),
                props,
                "no such property in %s: %s",
                beanClass.getSimpleName(),
                prop);
  }
}
