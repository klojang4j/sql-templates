package org.klojang.jdbc;

import java.sql.ResultSet;

import static java.lang.System.identityHashCode;
import static org.klojang.jdbc.BatchQuery.QueryId;
import static org.klojang.jdbc.x.Utils.CENTRAL_CLEANER;

// Despite its name, this is more like a ResultSet cache
final class QueryCache {

  private static final QueryCache instance = new QueryCache();

  static QueryCache getInstance() { return instance; }


  private final QueryCacheManager cacheManager;

  private QueryCache() {
    cacheManager = new QueryCacheManager();
    CENTRAL_CLEANER.register(this, cacheManager);
  }

  ResultSet getResultSet(QueryId id) {
    return cacheManager.get(id);
  }

  QueryId addQuery(SQLQuery query,
        long stayAliveSeconds,
        boolean closeConnection) {
    String hash = String.valueOf(identityHashCode(query.getResultSet()));
    QueryId id = QueryId.of(hash);
    LiveQuery liveQuery = new LiveQuery(query, stayAliveSeconds, closeConnection);
    cacheManager.add(id, liveQuery);
    return id;
  }

  void terminate(QueryId id) {
    cacheManager.terminate(id);
  }


  void terminateAll() {
    cacheManager.terminateAll();
  }


}
