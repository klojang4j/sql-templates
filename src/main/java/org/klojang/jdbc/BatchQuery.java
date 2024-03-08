package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.convert.NumberMethods;

import java.sql.Connection;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.List;

import static java.lang.System.identityHashCode;
import static org.klojang.jdbc.x.Strings.QUERY;

/**
 * Facilitates the processing of large query results in batches across multiple,
 * independent requests. These would typically be HTTP requests, but any stateless
 * request-response mechanism might want to use the functionality offered by the
 * {@code BatchInsert} class. The query result will be kept alive until all records have
 * been extracted from it. Once all records have been extracted, the associated JDBC
 * resources will be closed automatically. As soon as, and as long as there are any
 * persistent query results, a background thread will periodically check whether any of
 * them have gone stale. Stale query results will be closed and removed by this thread.
 * Once there are no more persistent query results, the thread will itself be terminated.
 *
 * @param <T> the type of the JavaBeans or records produced by the
 *       {@code BatchQuery}
 */
public final class BatchQuery<T> {

  /**
   * Registers the specified {@code SQLQuery} for batch processing and returns a
   * {@code QueryId}. The {@code QueryId} can be used to instantiate a {@code BatchInsert}
   * object, allowing it the identify and wrap itself around a {@link ResultSet}.
   * Equivalent to
   * {@link #register(SQLQuery, Duration) register(query, Duration.ofMinutes(10), true)}.
   * In other words, the client gets ten minutes to process a batch before it must request
   * the {@linkplain #nextBatch(int) next batch}. After that, the {@link ResultSet} will
   * be deemed stale. It will be closed and disposed of, and any subsequent call to
   * {@code nextBatch()} cause a {@link DatabaseException}.
   *
   * @param query the {@code SQLQuery} to be registered for batch processing
   * @return a {@code QueryId}
   */
  public static QueryId register(SQLQuery query) {
    return register(query, Duration.ofMinutes(10), true);
  }

  /**
   * Registers the specified {@code SQLQuery} for batch processing and returns a
   * {@code QueryId}. The {@code QueryId} can be used to instantiate a {@code BatchInsert}
   * object, allowing it the identify and wrap itself around a {@link ResultSet}.
   * Equivalent to
   * {@link #register(SQLQuery, Duration) register(query, stayAliveTime, true)}.
   *
   * @param query the {@code SQLQuery} to be registered for batch processing
   * @param stayAliveTime determines how long the query result should be kept alive
   *       between requests for new batches. If the time interval between any two
   *       consecutive requests is longer than the specified duration, the query result
   *       will be deemed stale and <i>Klojang JDBC</i> will close the associated JDBC
   *       resources and remove it.
   * @return a {@code QueryId}
   */
  public static QueryId register(SQLQuery query, Duration stayAliveTime) {
    return register(query, stayAliveTime, true);
  }

  /**
   * Registers the specified {@code SQLQuery} for batch processing and returns a
   * {@code QueryId}. The {@code QueryId} can be used to instantiate a {@code BatchInsert}
   * object, allowing it the identify and wrap itself around a {@link ResultSet}.
   *
   * @param query the {@code SQLQuery} to be registered for batch processing
   * @param stayAliveTime determines how long the query result should be kept alive
   *       between requests for new batches. If the time interval between any two
   *       consecutive requests is longer than the specified duration, the query result
   *       will be deemed stale and <i>Klojang JDBC</i> will close the associated JDBC
   *       resources and remove it.
   * @param closeConnection whether to close the JDBC connection once all records
   *       have been retrieved from the underlying {@link ResultSet}
   * @return
   */
  public static QueryId register(SQLQuery query,
        Duration stayAliveTime,
        boolean closeConnection) {
    Check.notNull(query, QUERY);
    Check.notNull(stayAliveTime, "stayAliveTime");
    ResultSet rs = query.execute();
    ResultSetCache cache = ResultSetCache.getInstance();
    Connection con = query.session.con;
    return cache.add(con, rs, stayAliveTime.getSeconds(), closeConnection);
  }

  /**
   * Terminates the processing of all query results. Any subsequent call on any existing
   * {@code BatchQuery} instances to {@link #nextBatch(int)} will cause a
   * {@link DatabaseException}.
   */
  public static void terminateAll() {
    ResultSetCache.getInstance().clearCache();
  }

  private final QueryId queryId;
  private final ExtractorFactory<T> factory;

  /**
   * Instantiates a new {@code BatchQuery} object.
   *
   * @param queryId the ID of the query result to be processed by the
   *       {@code BatchQuery} object
   * @param factory a factory for the {@link BeanExtractor} that will process the
   *       {@code ResultSet}
   */
  public BatchQuery(QueryId queryId, ExtractorFactory<T> factory) {
    this.queryId = queryId;
    this.factory = factory;
  }

  /**
   * Retrieves the next batch of records from the query result and converts them into
   * instances of type {@code <T>}. A {@link DatabaseException} is thrown if it has taken
   * too long for this method to be called since the previous time it was called (too long
   * as defined by the {@code stayAliveTime} argument passed to the
   * {@link #register(SQLQuery, Duration, boolean) register()} method).
   *
   * @param batchSize the number of records to retrieve
   * @return the next batch of records, converted into instances of type {@code <T>}
   */
  public List<T> nextBatch(int batchSize) {
    ResultSetCache cache = ResultSetCache.getInstance();
    ResultSet rs = ResultSetCache.getInstance().get(queryId);
    BeanExtractor<T> extractor = factory.getExtractor(rs);
    List<T> beans = extractor.extract(batchSize);
    if (extractor.isEmpty()) {
      cache.remove(queryId);
    }
    return beans;
  }

  /**
   * Terminates the processing of the query result. All subsequent calls to
   * {@link #nextBatch(int)} will cause a {@link DatabaseException}. Will close the
   * underlying {@link ResultSet} and possibly also the associated JDBC connection (see
   * {@link #register(SQLQuery, Duration, boolean)}). You do not need to call this method
   * if you process the query result all the way to the last row, but you might want to
   * call this in exceptional situations where you need to abort the query.
   */
  public void terminate() {
    ResultSetCache.getInstance().remove(queryId);
  }

  /**
   * Functions as an identifier for a persistent query result. A {@code QueryId} (or
   * rather it string representation) is meant to be ping-ponged back and forth between
   * client and server, for example via a URL query parameter and response header,
   * respectively. On the server side it is used to instantiate a {@code BatchInsert}
   * object, allowing it to identify and wrap itself around the query result.
   */
  public static final class QueryId {

    private final int id;

    private QueryId(int id) { this.id = id; }

    QueryId(ResultSet rs) { this(identityHashCode(rs)); }

    /**
     * Creates a {@code QueryId} from the specified string representation
     *
     * @param id the string representation of a {@code QueryId}
     * @return a {@code QueryId} from the specified string representation
     */
    public static QueryId of(String id) {
      Check.notNull(id);
      return new QueryId(NumberMethods.parseInt(id));
    }

    /**
     * Returns the hash code of this {@code QueryId}.
     *
     * @return the hash code of this {@code QueryId}
     */
    public int hashCode() { return id; }

    /**
     * Determines whether this {@code QueryId} equals the specified object.
     *
     * @param obj the object to compare this {@code QueryId} with
     * @return whether this {@code QueryId} equals the specified object
     */
    @Override
    public boolean equals(Object obj) {
      return this == obj || (obj instanceof QueryId qid && id == qid.id);
    }

    /**
     * Returns the string representation of this {@code QueryId}.
     *
     * @return the string representation of this {@code QueryId}
     */
    public String toString() { return String.valueOf(id); }
  }

}