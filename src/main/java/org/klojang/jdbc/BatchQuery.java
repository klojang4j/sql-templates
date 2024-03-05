package org.klojang.jdbc;

import org.klojang.convert.NumberMethods;

import java.sql.Connection;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.List;

import static java.lang.System.identityHashCode;

public final class BatchQuery<T> {

  public static final class QueryId {

    public static QueryId of(String id) {
      return new QueryId(NumberMethods.parseInt(id));
    }

    private final int id;

    private QueryId(int id) { this.id = id; }

    QueryId(ResultSet rs) { this(identityHashCode(rs)); }

    public int hashCode() { return id; }

    public boolean equals(Object obj) {
      return this == obj || (obj instanceof QueryId qid && id == qid.id);
    }

    public String toString() { return String.valueOf(id); }
  }

  public static QueryId register(SQLQuery query) {
    return register(query, Duration.ofMinutes(10), true);
  }

  public static QueryId register(SQLQuery query, Duration stayAliveTime) {
    return register(query, stayAliveTime, true);
  }

  public static QueryId register(SQLQuery query,
        Duration stayAliveTime,
        boolean closeConnection) {
    ResultSet rs = query.execute();
    ResultSetCache cache = ResultSetCache.getInstance();
    Connection con = query.session.con;
    return cache.add(con, rs, stayAliveTime.getSeconds(), closeConnection);
  }

  public static void terminateAll() {
    ResultSetCache.getInstance().clearCache();
  }

  private final QueryId queryId;
  private final BeanExtractorFactory<T> factory;

  public BatchQuery(QueryId queryId, BeanExtractorFactory<T> factory) {
    this.queryId = queryId;
    this.factory = factory;
  }

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

  public void terminate() {
    ResultSetCache.getInstance().remove(queryId);
  }

}
