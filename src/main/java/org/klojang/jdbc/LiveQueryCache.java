package org.klojang.jdbc;

import java.sql.ResultSet;

import static java.lang.System.identityHashCode;
import static org.klojang.jdbc.BatchQuery.QueryId;
import static org.klojang.jdbc.x.Utils.CENTRAL_CLEANER;

final class LiveQueryCache {

  private static final LiveQueryCache instance = new LiveQueryCache();

  static LiveQueryCache getInstance() { return instance; }


  private final LiveQueryBroker broker;

  private LiveQueryCache() {
    broker = new LiveQueryBroker();
    CENTRAL_CLEANER.register(this, broker);
  }

  ResultSet getResultSet(QueryId id) {
    return broker.getResultSet(id);
  }
  SQLQuery getSQLQuery(QueryId id) {
    return broker.getSQLQuery(id);
  }

  QueryId addQuery(SQLQuery query,
        long stayAliveSeconds,
        boolean closeConnection) {
    var hash = identityHashCode(query.getResultSet());
    var id = QueryId.of(String.valueOf(hash));
    var liveQuery = new LiveQuery(query, stayAliveSeconds, closeConnection);
    broker.add(id, liveQuery);
    return id;
  }

  void terminate(QueryId id) {
    broker.terminate(id);
  }

  void terminateAll() {
    broker.terminateAll();
  }


}
