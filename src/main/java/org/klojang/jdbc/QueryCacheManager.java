package org.klojang.jdbc;

import org.klojang.jdbc.x.Err;
import org.klojang.jdbc.x.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import static org.klojang.check.CommonChecks.keyIn;
import static org.klojang.check.CommonChecks.notNull;
import static org.klojang.jdbc.BatchQuery.QueryId;

final class QueryCacheManager implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(QueryCacheManager.class);
  private static final long CHECK_INTERVAL = 2 * 60 * 1000; // every 2 minutes

  private final Map<QueryId, LiveQuery> cache = new HashMap<>();

  private Thread cleaner;

  ResultSet get(QueryId id) {
    synchronized (cache) {
      LiveQuery query = cache.get(id);
      Utils.check(query).is(notNull(), Err.STALE_QUERY, id);
      return query.getResultSet();
    }
  }

  void add(QueryId id, LiveQuery query) {
    synchronized (cache) {
      Utils.check(id).isNot(keyIn(), cache, "query already registered (id={})", id);
      LOG.trace("Registering query (id={})", id);
      cache.put(id, query);
      if (cleaner == null) {
        startCleaning();
      }
    }
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

  @Override
  public void run() {
    stopCleaning();
    cache.values().forEach(LiveQuery::kill);
    cache.clear();
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
