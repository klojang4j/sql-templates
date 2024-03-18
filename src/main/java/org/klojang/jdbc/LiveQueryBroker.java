package org.klojang.jdbc;

import org.klojang.jdbc.x.Err;
import org.klojang.jdbc.x.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.identityHashCode;
import static org.klojang.check.CommonChecks.keyIn;
import static org.klojang.check.CommonChecks.notNull;
import static org.klojang.jdbc.BatchQuery.QueryId;

final class LiveQueryBroker {

  private static final Logger LOG = LoggerFactory.getLogger(LiveQueryBroker.class);
  private static final long CHECK_INTERVAL = 2 * 60 * 1000; // every 2 minutes
  private static final LiveQueryBroker INSTANCE = new LiveQueryBroker();

  static LiveQueryBroker getInstance() { return INSTANCE; }


  private final Map<QueryId, LiveQuery> cache = new HashMap<>();

  private Thread cleaner;

  ResultSet getResultSet(QueryId id) {
    synchronized (cache) {
      LiveQuery query = cache.get(id);
      Utils.check(query).is(notNull(), Err.STALE_QUERY, id);
      return query.getResultSet();
    }
  }

  SQLQuery getQuery(QueryId id) {
    synchronized (cache) {
      LiveQuery query = cache.get(id);
      Utils.check(query).is(notNull(), Err.STALE_QUERY, id);
      return query.getSQLQuery();
    }
  }

  QueryId register(SQLQuery query, long stayAliveSeconds, boolean closeConnection) {
    var hash = identityHashCode(query.getResultSet());
    var id = QueryId.of(String.valueOf(hash));
    var liveQuery = new LiveQuery(query, stayAliveSeconds, closeConnection);
    synchronized (cache) {
      Utils.check(id).isNot(keyIn(), cache, "query already registered (id={})", id);
      LOG.trace("Registering query (id={})", id);
      cache.put(id, liveQuery);
      if (cleaner == null) {
        startCleaning();
      }
    }
    return id;
  }

  void terminate(QueryId id) {
    synchronized (cache) {
      LiveQuery query = cache.remove(id);
      if (query != null) {
        query.terminate(id);
        if (cache.isEmpty()) {
          stopCleaning();
        }
      }
    }
  }

  void terminateAll() {
    stopCleaning();
    synchronized (cache) {
      LOG.trace("Terminating all queries");
      cache.forEach((id, query) -> query.terminate(id));
      cache.clear();
    }
  }

  private void startCleaning() {
    cleaner = Thread.ofVirtual().start(this::removeStaleQueries);
  }

  private void stopCleaning() {
    if (cleaner != null) {
      try {
        cleaner.interrupt();
      } finally {
        cleaner = null;
      }
    }
  }

  private void removeStaleQueries() {
    while (true) {
      try {
        Thread.sleep(CHECK_INTERVAL);
      } catch (InterruptedException e) {
        LOG.trace("Aborting staleness check");
        cleaner = null;
        break;
      }
      synchronized (cache) {
        var iterator = cache.entrySet().iterator();
        while (iterator.hasNext()) {
          var entry = iterator.next();
          var id = entry.getKey();
          var query = entry.getValue();
          if (query.isStale()) {
            query.terminate(id);
            iterator.remove();
          }
        }
        if (cache.isEmpty()) {
          cleaner = null;
          break;
        }
      }
    }
  }
}
