package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.JDBC;
import org.klojang.jdbc.x.Utils;
import org.klojang.jdbc.x.sql.BatchInsertConfig;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static java.sql.Statement.NO_GENERATED_KEYS;
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.util.stream.Collectors.joining;
import static org.klojang.check.CommonExceptions.STATE;
import static org.klojang.util.ArrayMethods.EMPTY_LONG_ARRAY;
import static org.klojang.util.StringMethods.append;

/**
 * <p>{@code SQLBatchInsert} specializes in saving large batches of
 * JavaBeans or records to the database. Instances are configured and obtained via a
 * {@link BatchInsertBuilder}. They are not underpinned by
 * {@linkplain java.sql.PreparedStatement prepared statements}. Yet, they provide just as
 * much protection against SQL injection, as they will process each and every value in the
 * batch using {@link Quoter#quoteValue(Object) Quoter.quoteValue()}. This method
 * ultimately relies on
 * {@link Statement#enquoteLiteral(String) Statement.enquoteLiteral()} &#8212; in other
 * words, the JDBC driver's own escape-and-quote mechanism.
 *
 * <h2>Batch Insert Options</h2>
 *
 * <p><i>Klojang JDBC</i> provides three options for saving batches of beans:
 *
 * <ol>
 *   <li>via {@link SQLInsert#insertBatch(List) SQLInsert.insertBatch()}
 *   <li>via a {@code SQLBatchInsert} instance
 *   <li>via {@link SQLSession#setValues(List) SQLSession.setValues()}
 * </ol>
 *
 * <p>The first option is convenient for small batches, but not very efficient for large
 * ones as it, in fact, saves the beans one at a time. The second and third option are
 * equivalent, although, currently, their implementation details do differ.
 * {@code SQLBatchInsert} instances are configured through a fluent API, which you may
 * find attractive. Batch inserts via {@code SQLSession.setValues()} are more in line
 * with the <i>Klojang JDBC</i> idiom in general. Which one you choose largely is a matter
 * of taste.
 *
 * @param <T> the type of the JavaBeans or records to save to the database.
 * @see BatchInsertBuilder
 * @see SQL#batchInsert()
 */
@SuppressWarnings({"resource"})
public final class SQLBatchInsert<T> {

  private static final String RECORDS_DONT_HAVE_SETTERS = "cannot set id on record types";

  private final BatchInsertConfig<T> cfg;
  private final String sqlBase;

  SQLBatchInsert(BatchInsertConfig<T> cfg) {
    this.cfg = cfg;
    this.sqlBase = getSqlBase(cfg);
  }

  /**
   * Saves the provided beans to the database.
   *
   * @param beans the beans to save
   */
  public void insertBatch(List<T> beans) {
    Check.notNull(beans);
    if (!beans.isEmpty()) {
      int chunkSize = cfg.chunkSize() == -1 ? beans.size() : cfg.chunkSize();
      insertBatch(beans, chunkSize);
    }
  }

  /**
   * Saves the provided beans to the database and returns the keys generated by the
   * database.
   *
   * @param beans the beans to save
   * @return the keys generated by the database
   */
  public long[] insertBatchAndGetIDs(List<T> beans) {
    Check.notNull(beans);
    if (beans.isEmpty()) {
      return EMPTY_LONG_ARRAY;
    }
    int chunkSize = cfg.chunkSize() == -1 ? beans.size() : cfg.chunkSize();
    return insertBatchAndGetIDs(beans, chunkSize);
  }

  /**
   * Saves the specified JavaBeans to the database and sets the specified ID property in
   * each of them to the key generated by the database. Obviously, the ID property must be
   * mutable in that case, so {@code <T>} must not be a {@code record} type.
   *
   * @param idProperty the name of the property corresponding to the primary key
   * @param beans the beans to save
   */
  public void insertBatchAndSetIDs(String idProperty, List<T> beans) {
    Check.notNull(beans);
    Class<T> clazz = cfg.reader().getBeanClass();
    Check.on(STATE, clazz).isNot(Class::isRecord, RECORDS_DONT_HAVE_SETTERS);
    if (!beans.isEmpty()) {
      int chunkSize = cfg.chunkSize() == -1 ? beans.size() : cfg.chunkSize();
      insertBatchAndSetIDs(beans, idProperty, chunkSize);
    }
  }

  private void insertBatch(List<T> beans, int chunkSize) {
    for (int i = 0; i < beans.size(); i += chunkSize) {
      int j = Math.min(beans.size(), i + chunkSize);
      try {
        insertChunk(beans.subList(i, j));
      } catch (Throwable t) {
        throw Utils.wrap(t);
      }
    }
  }

  private long[] insertBatchAndGetIDs(List<T> beans, int chunkSize) {
    long[] allKeys = new long[beans.size()];
    for (int i = 0; i < beans.size(); i += chunkSize) {
      int j = Math.min(beans.size(), i + chunkSize);
      try {
        long[] keys = insertChunkAndGetIDs(beans.subList(i, j));
        System.arraycopy(keys, 0, allKeys, i, j);
      } catch (Throwable t) {
        throw Utils.wrap(t);
      }
    }
    return allKeys;
  }

  private void insertBatchAndSetIDs(List<T> beans, String idProperty, int chunkSize) {
    for (int i = 0; i < beans.size(); i += chunkSize) {
      int j = Math.min(beans.size(), i + chunkSize);
      try {
        insertChunkAndSetIDs(beans.subList(i, j), idProperty);
      } catch (Throwable t) {
        throw Utils.wrap(t);
      }
    }
  }

  private void insertChunk(List<T> beans) throws Throwable {
    StringBuilder sql = new StringBuilder(guessSize(beans));
    sql.append(sqlBase);
    try (Statement stmt = cfg.connection().createStatement()) {
      addRows(sql, stmt, beans);
      stmt.executeUpdate(sql.toString(), NO_GENERATED_KEYS);
    }
    commit();
  }

  private long[] insertChunkAndGetIDs(List<T> beans) throws Throwable {
    long[] keys;
    StringBuilder sql = new StringBuilder(guessSize(beans));
    sql.append(sqlBase);
    try (Statement stmt = cfg.connection().createStatement()) {
      addRows(sql, stmt, beans);
      stmt.executeUpdate(sql.toString(), RETURN_GENERATED_KEYS);
      keys = JDBC.getGeneratedKeys(stmt, beans.size());
    }
    commit();
    return keys;
  }

  private void insertChunkAndSetIDs(List<T> beans, String idProperty) throws Throwable {
    StringBuilder sql = new StringBuilder(guessSize(beans));
    sql.append(sqlBase);
    try (Statement stmt = cfg.connection().createStatement()) {
      addRows(sql, stmt, beans);
      stmt.executeUpdate(sql.toString(), RETURN_GENERATED_KEYS);
      long[] keys = JDBC.getGeneratedKeys(stmt, beans.size());
      for (int i = 0; i < keys.length; ++i) {
        JDBC.setID(beans.get(i), idProperty, keys[i]);
      }
    }
    commit();
  }

  private void addRows(StringBuilder sql, Statement stmt, List<T> beans) {
    int i = 0;
    for (T bean : beans) {
      if (i++ > 0) {
        sql.append(',');
      }
      addRow(sql, stmt, bean);
    }
  }

  private void addRow(StringBuilder sql, Statement stmt, T bean) {
    BatchInsertConfig<T> cfg = this.cfg;
    Quoter quoter = new Quoter(stmt);
    String[] props = cfg.reader().getReadableProperties().toArray(String[]::new);
    List<Object> values = cfg.reader().readAllProperties(bean);
    sql.append('(');
    for (int i = 0; i < props.length; ++i) {
      if (i > 0) {
        sql.append(',');
      }
      Object val = cfg.processor().process(bean, props[i], values.get(i), quoter);
      sql.append(quoter.quoteValue(val));
    }
    sql.append(')');
  }

  private void commit() throws SQLException {
    if (cfg.commitPerChunk() && !cfg.connection().getAutoCommit()) {
      cfg.connection().commit();
    }
  }

  private static String getSqlBase(BatchInsertConfig<?> cfg) {
    String cols = cfg.reader()
          .getReadableProperties()
          .stream()
          .map(cfg.mapper()::map)
          .collect(joining(","));
    StringBuilder sb = new StringBuilder(cols.length() + 40);
    append(sb, "INSERT INTO ", cfg.tableName(), '(', cols, ")VALUES");
    return sb.toString();
  }

  private int guessSize(List<T> beans) {
    return 50 + (cfg.reader().getReadableProperties().size() * beans.size() * 12);
  }

}
