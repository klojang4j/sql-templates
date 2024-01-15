package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.invoke.Getter;
import org.klojang.jdbc.x.JDBC;
import org.klojang.jdbc.x.sql.BatchInsertConfig;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import static java.sql.Statement.NO_GENERATED_KEYS;
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.util.stream.Collectors.joining;
import static org.klojang.check.CommonExceptions.STATE;
import static org.klojang.util.ArrayMethods.EMPTY_LONG_ARRAY;
import static org.klojang.util.StringMethods.append;

/**
 * <p>{@code SQLBatchInsert} specializes in saving potentially very large batches of
 * JavaBeans to be saved to the database. Contrary to {@link SQLInsert}
 * {@code SQLBatchInsert} is not a subclass of {@link SQLStatement}. It is not underpinned
 * by prepared statements (i.e. the {@link PreparedStatement} class). It still protects
 * against SQL injection, though, as all values except numbers and booleans are quoted and
 * escaped using the target database's quoting and escaping rules.
 *
 * <p>{@code SQLBatchInsert} implements {@link AutoCloseable} in order to stay aligned
 * with {@link SQLStatement} and its subclasses, and more specifically so you can easily
 * switch between {@link SQLInsert} and {@code SQLBatchInsert} within your code. However,
 * its {@code close()} method currently is a no-op. It is not necessary to use a
 * try-with-resource block with {@code SQLBatchInsert}.
 *
 * @param <T> the type of the JavaBeans to save to the database.
 * @see SQLInsertBuilder
 * @see SQLInsert
 * @see SQL#expression(String)
 * @see Quoter
 */
public final class SQLBatchInsert<T> implements AutoCloseable {

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
   * each of them to the key generated by the database.
   *
   * @param idProperty the name of the property corresponding to the primary key
   * @param beans the beans to save
   */
  public void insertBatchAndSetIDs(String idProperty, List<T> beans) {
    Check.notNull(beans);
    Check.on(STATE, cfg.beanClass()).isNot(Class::isRecord, RECORDS_DONT_HAVE_SETTERS);
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
        throw new KlojangSQLException(t);
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
        throw new KlojangSQLException(t);
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
        throw new KlojangSQLException(t);
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

  private void addRows(StringBuilder sql, Statement stmt, List<T> beans)
        throws Throwable {
    int i = 0;
    for (T bean : beans) {
      if (i++ > 0) {
        sql.append(',');
      }
      addRow(sql, stmt, bean);
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void addRow(StringBuilder sql, Statement stmt, T bean) throws Throwable {
    sql.append('(');
    for (int i = 0; i < cfg.getters().length; ++i) {
      if (i > 0) {
        sql.append(',');
      }
      Getter getter = cfg.getters()[i];
      Transformer transformer = cfg.transformers()[i];
      Object value = getter.read(bean);
      if (transformer != null) {
        value = transformer.transform(bean, value, new Quoter(stmt));
      }
      sql.append(JDBC.quote(stmt, value));
    }
    sql.append(')');
  }

  /**
   * This is a no-op for {@code SQLBatchInsert}.
   */
  @Override
  public void close() { }

  private void commit() throws SQLException {
    if (cfg.commitPerChunk() && !cfg.connection().getAutoCommit()) {
      cfg.connection().commit();
    }
  }

  private static String getSqlBase(BatchInsertConfig<?> cfg) {
    String cols = Arrays.stream(cfg.getters())
          .map(Getter::getProperty)
          .map(cfg.mapper()::map)
          .collect(joining(","));
    StringBuilder sb = new StringBuilder(cols.length() + 40);
    append(sb, "INSERT INTO ", cfg.tableName(), '(', cols, ")VALUES");
    return sb.toString();
  }

  private int guessSize(List<T> beans) {
    return 50 + (cfg.getters().length * beans.size() * 12);
  }

}
