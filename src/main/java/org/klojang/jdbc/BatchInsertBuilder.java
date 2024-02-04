package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.invoke.BeanReader;
import org.klojang.invoke.IncludeExclude;
import org.klojang.jdbc.x.sql.BatchInsertConfig;
import org.klojang.templates.NameMapper;

import java.sql.Connection;

import static org.klojang.check.CommonChecks.gt;
import static org.klojang.check.CommonChecks.notNull;
import static org.klojang.check.CommonExceptions.STATE;
import static org.klojang.invoke.IncludeExclude.EXCLUDE;
import static org.klojang.invoke.IncludeExclude.INCLUDE;
import static org.klojang.util.ArrayMethods.EMPTY_STRING_ARRAY;

/**
 * A builder class for {@link SQLBatchInsert} instances. {@code BatchInsertBuilder}
 * instances are obtained via {@link SQL#configureBatchInsert()}.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class BatchInsertBuilder {

  private BeanValueProcessor processor = BeanValueProcessor.identity();
  private IncludeExclude includeExclude = INCLUDE;
  private String[] properties = EMPTY_STRING_ARRAY;
  private NameMapper nameMapper = NameMapper.AS_IS;
  private int chunkSize = -1;
  boolean commitPerChunk = true;

  private Class beanClass;
  private String tableName;

  BatchInsertBuilder() { }

  /**
   * Sets the type of the beans or records to be saved.
   *
   * @param beanClass the type of the beans or records to be saved
   * @return this {@code BatchInsertBuilder}
   */
  public BatchInsertBuilder of(Class<?> beanClass) {
    this.beanClass = Check.notNull(beanClass).ok();
    return this;
  }

  /**
   * Sets the table name to insert the data into. If not specified, this defaults to the
   * simple class name of the bean class.
   *
   * @param tableName the table name to insert the data into
   * @return this {@code BatchInsertBuilder}
   */
  public BatchInsertBuilder into(String tableName) {
    this.tableName = Check.notNull(tableName).ok();
    return this;
  }

  /**
   * Sets the properties (and corresponding columns) to exclude from the INSERT statement.
   * You would most likely at least want to exclude the property corresponding to the
   * auto-generated key column. A call to {@code excluding()} will overwrite any previous
   * calls to either {@code including()} or {@code excluding()}.
   *
   * @param properties the properties and (corresponding columns) to exclude from
   *       the INSERT statement
   * @return this {@code BatchInsertBuilder}
   */
  public BatchInsertBuilder excluding(String... properties) {
    this.properties = Check.notNull(properties).ok();
    this.includeExclude = EXCLUDE;
    return this;
  }

  /**
   * Sets the properties (and corresponding columns) to include in the INSERT statement. A
   * call to {@code including()} will overwrite any previous calls to either
   * {@code including()} or {@code excluding()}.
   *
   * @param properties the properties and (corresponding columns) to include in the
   *       INSERT statement
   * @return this {@code BatchInsertBuilder}
   */
  public BatchInsertBuilder including(String... properties) {
    this.properties = Check.notNull(properties).ok();
    this.includeExclude = INCLUDE;
    return this;
  }

  /**
   * Sets number of beans that will be saved at a time. By default the entire batch will
   * be saved at once. Make sure this does not exceed the limits of your database or JDBC
   * driver.
   *
   * @param chunkSize the number of beans that will be saved at a time
   * @return this {@code BatchInsertBuilder}
   */
  public BatchInsertBuilder withChunkSize(int chunkSize) {
    this.chunkSize = Check.that(chunkSize).is(gt(), 0).ok();
    return this;
  }

  /**
   * Specifies whether to issue a database commit directly after a chunk of beans has been
   * saved to the database. If not, you must issue the commits yourself, if and when
   * necessary. The default behaviour is to issue a commit.
   *
   * @param commitPerChunk whether to commit after een chunk of beans has been saved
   *       to the database
   * @return this {@code BatchInsertBuilder}
   */
  public BatchInsertBuilder withCommitPerChunk(boolean commitPerChunk) {
    this.commitPerChunk = commitPerChunk;
    return this;
  }

  /**
   * Specifies the {@code BeanValueProcessor} to use to selectively convert values in bean
   * batches. If not specified, {@link BeanValueProcessor#identity()} is used. This
   * basically is a no-op processor.
   *
   * @param processor the {@code BeanValueProcessor} to use
   * @return this {@code BatchInsertBuilder}
   */
  public BatchInsertBuilder withValueProcessor(BeanValueProcessor<?> processor) {
    this.processor = Check.notNull(processor, "processor").ok();
    return this;
  }

  /**
   * Sets the property-to-column mapper to be used when mapping bean properties to column
   * names. Beware of the direction of the mappings: <i>from</i> bean properties
   * <i>to</i> column names.
   *
   * @param propertyToColumnMapper the property-to-column mapper
   * @return this {@code SQLBatchInsertBuilder}
   */
  public BatchInsertBuilder withNameMapper(NameMapper propertyToColumnMapper) {
    Check.notNull(propertyToColumnMapper);
    this.nameMapper = propertyToColumnMapper;
    return this;
  }


  /**
   * Creates and returns a {@code SQLBatchInsert} instance using the input provided via
   * the other methods
   *
   * @param con the JDBC {@code Connection} to use for the INSERT statement
   * @param <T> the type of the beans or records to be persisted by the
   *       {@code SQLBatchInsert} instance
   * @return a {@code SQLBatchInsert} instance
   */
  public <T> SQLBatchInsert<T> prepare(Connection con) {
    Check.notNull(con);
    Check.on(STATE, beanClass, "beanClass").is(notNull());
    BeanReader reader = new BeanReader<>(beanClass, includeExclude, properties);
    BatchInsertConfig cfg = new BatchInsertConfig<>(con,
          reader,
          processor,
          nameMapper,
          tableName,
          chunkSize,
          commitPerChunk);
    return new SQLBatchInsert<>(cfg);
  }

}
