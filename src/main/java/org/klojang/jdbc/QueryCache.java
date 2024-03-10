package org.klojang.jdbc;

import java.sql.ResultSet;

import static java.lang.System.identityHashCode;
import static org.klojang.jdbc.BatchQuery.QueryId;
import static org.klojang.jdbc.x.Utils.CENTRAL_CLEANER;

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
    var hash = identityHashCode(query.getResultSet());
    var id = QueryId.of(String.valueOf(hash));
    var liveQuery = new LiveQuery(query, stayAliveSeconds, closeConnection);
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
