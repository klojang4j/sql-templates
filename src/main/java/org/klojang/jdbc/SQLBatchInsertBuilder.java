package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.invoke.Getter;
import org.klojang.invoke.GetterFactory;
import org.klojang.jdbc.x.sql.BatchInsertConfig;
import org.klojang.templates.NameMapper;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static org.klojang.check.CommonChecks.*;
import static org.klojang.check.CommonExceptions.STATE;
import static org.klojang.check.CommonProperties.mapSize;
import static org.klojang.check.Tag.PROPERTY;
import static org.klojang.util.ObjectMethods.isEmpty;

/**
 * A builder class for {@link SQLBatchInsert} instances. {@code SQLBatchInsertBuilder}
 * instances are obtained via {@link SQL#prepareBatchInsert()}.
 *
 * @param <T> the type of the beans to be saved
 */
public final class SQLBatchInsertBuilder<T> {

    private final Map<String, Transformer<T>> transformers = new HashMap<>();

    private NameMapper nameMapper = NameMapper.AS_IS;
    private int chunkSize = -1;
    boolean commitPerChunk = true;

    private Class<T> beanClass;
    private String tableName;
    private String[] properties;
    private boolean exclude;

    SQLBatchInsertBuilder() {}

    /**
     * Sets the type of the beans to be saved.
     *
     * @param beanClass the type of the beans to be saved
     * @return this {@code SQLBatchInsertBuilder}
     */
    public SQLBatchInsertBuilder<T> of(Class<T> beanClass) {
        this.beanClass = Check.notNull(beanClass).ok();
        return this;
    }

    /**
     * Sets the table name to insert the data into. If not specified, this defaults to the
     * simple class name of the bean class.
     *
     * @param tableName the table name to insert the data into
     * @return this {@code SQLBatchInsertBuilder}
     */
    public SQLBatchInsertBuilder<T> into(String tableName) {
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
     * @return this {@code SQLBatchInsertBuilder}
     */
    public SQLBatchInsertBuilder<T> excluding(String... properties) {
        Check.that(properties).is(deepNotEmpty());
        this.properties = properties;
        this.exclude = true;
        return this;
    }

    /**
     * Sets the properties and (corresponding columns) to include in the INSERT statement.
     * It makes no sense to call both this method and the {@link #excluding(String...)} on
     * the same {@code SQLBatchInsertBuilder} instance. The last call will override the
     * effect of any previous calls to {@code including()} and {@code excluding()}.
     *
     * @param properties the properties and (corresponding columns) to include in the
     * INSERT statement
     * @return this {@code SQLBatchInsertBuilder}
     */
    public SQLBatchInsertBuilder<T> including(String... properties) {
        Check.that(properties).is(deepNotEmpty());
        this.properties = properties;
        this.exclude = false;
        return this;
    }

    /**
     * Sets the property-to-column mapper to be used when mapping bean properties to
     * column names. Beware of the direction of the mappings: <i>from</i> bean properties
     * <i>to</i> column names.
     *
     * @param propertyToColumnMapper the property-to-column mapper
     * @return this {@code SQLBatchInsertBuilder}
     */
    public SQLBatchInsertBuilder<T> withNameMapper(NameMapper propertyToColumnMapper) {
        Check.notNull(propertyToColumnMapper);
        this.nameMapper = propertyToColumnMapper;
        return this;
    }

    /**
     * Sets number of beans that will be saved at a time. By default the entire batch will
     * be saved at once. Make sure this does not exceed the limits of your database or
     * JDBC client.
     *
     * @param chunkSize the number of beans that will be saved at a time
     * @return this {@code SQLBatchInsertBuilder}
     */
    public SQLBatchInsertBuilder<T> withChunkSize(int chunkSize) {
        Check.that(chunkSize).is(gt(), 0);
        this.chunkSize = chunkSize;
        return this;
    }

    /**
     * Specifies whether to issue a database commit directly after een chunk of beans has
     * been saved to the database. If not, you must issue the commits yourself if and when
     * necessary. The default behaviour is to issue a commit.
     *
     * @param commitPerChunk whether to commit after een chunk of beans has been saved to
     * the database
     * @return this {@code SQLBatchInsertBuilder}
     */
    public SQLBatchInsertBuilder<T> withCommitPerChunk(boolean commitPerChunk) {
        this.commitPerChunk = commitPerChunk;
        return this;
    }

    /**
     * Specifies a transformer function for the specified property. The function is passed
     * the bean to be saved and the value of the property. It must return the value to be
     * saved for that property. If the return value is anything other than {@code null}, a
     * {@link Number} or an {@link SQL#expression(String) SQL expression}, it is
     * stringified using {@code toString()} and then quoted and escaped using the
     * database's quoting and escaping rules.
     *
     * @param property the property whose value is to be transformed
     * @param transformer the transformation functions
     * @return this {@code SQLBatchInsertBuilder}
     * @see java.sql.Statement#enquoteLiteral(String)
     */
    public SQLBatchInsertBuilder<T> withTransformer(
            String property,
            Transformer<T> transformer) {
        Check.notNull(property, PROPERTY);
        Check.notNull(transformer, "transformer");
        this.transformers.put(property, transformer);
        return this;
    }

    /**
     * Creates and returns a {@code SQLBatchInsert} instance using the input provided via
     * the other methods
     *
     * @param con the JDBC {@code Connection} to use for the INSERT statement
     * @return a {@code SQLInsert} instance
     */
    @SuppressWarnings("rawtypes")
    public SQLBatchInsert<T> prepare(Connection con) {
        Check.notNull(con);
        Check.on(STATE, beanClass, "beanClass").is(notNull());
        Map<String, Getter> getters = GetterFactory.INSTANCE.getGetters(beanClass, true);
        Getter[] getterArray;
        if (isEmpty(properties)) {
            getterArray = getters.values().toArray(Getter[]::new);
        } else {
            for (String prop : properties) {
                Check.that(prop).is(keyIn(), getters, noSuchProperty(prop));
            }
            Map<String, Getter> tmp = HashMap.newHashMap(getters.size());
            tmp.putAll(getters);
            if (exclude) {
                tmp.keySet().removeAll(Set.of(properties));
            } else {
                tmp.keySet().retainAll(Set.of(properties));
            }
            Check.that(tmp).has(mapSize(), gt(), 0,
                    () -> new KlojangSQLException("no properties/columns selected"));
            getterArray = tmp.values().toArray(Getter[]::new);
        }
        Transformer[] transformerArray = Arrays.stream(getterArray)
                .map(Getter::getProperty)
                .map(transformers::get)
                .toArray(Transformer[]::new);
        BatchInsertConfig<T> cfg = new BatchInsertConfig<>(
                con,
                beanClass,
                tableName,
                chunkSize,
                commitPerChunk,
                getterArray,
                transformerArray,
                nameMapper);
        return new SQLBatchInsert<>(cfg);
    }

    private Supplier<KlojangSQLException> noSuchProperty(String prop) {
        String fmt = "no such property in class %s: %s";
        String msg = String.format(fmt, beanClass.getSimpleName(), prop);
        return () -> new KlojangSQLException(msg);
    }
}
