package org.klojang.jdbc;

import org.klojang.jdbc.x.Err;
import org.klojang.jdbc.x.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.klojang.check.CommonChecks.keyIn;
import static org.klojang.check.CommonChecks.notNull;
import static org.klojang.jdbc.BatchQuery.QueryId;

final class ResultSetCache {

  private static final class ResultSetInfo {
    final Connection con;
    final ResultSet rs;
    final long stayAliveSeconds;
    final boolean closeConnection;

    long lastRequested;

    ResultSetInfo(Connection con,
          ResultSet rs,
          long stayAliveSeconds,
          boolean closeConnection) {
      this.con = con;
      this.rs = rs;
      this.stayAliveSeconds = stayAliveSeconds;
      this.closeConnection = closeConnection;
      this.lastRequested = Instant.now().getEpochSecond();
    }
  }

  private static final Logger LOG = LoggerFactory.getLogger(ResultSetCache.class);
  private static final long CHECK_INTERVAL = 2 * 60 * 1000; // every 2 minutes

  private static final ResultSetCache instance = new ResultSetCache();

  static ResultSetCache getInstance() {
    return instance;
  }

  private final Map<QueryId, ResultSetInfo> cache = new HashMap<>();

  private Thread cleaner;

  private ResultSetCache() { }

  ResultSet get(QueryId id) {
    synchronized (cache) {
      ResultSetInfo info = cache.get(id);
      Utils.check(info).is(notNull(), Err.STALE_QUERY, id);
      info.lastRequested = Instant.now().getEpochSecond();
      return info.rs;
    }
  }

  QueryId add(Connection con,
        ResultSet rs,
        long stayAliveSeconds,
        boolean closeConnection) {
    QueryId id = new QueryId(rs);
    synchronized (cache) {
      Utils.check(id).isNot(keyIn(), cache, "query with id {} already registered", id);
      LOG.trace("Registering query (id={})", id);
      cache.put(id, new ResultSetInfo(con, rs, stayAliveSeconds, closeConnection));
      if (cleaner == null) {
        cleaner = Thread.ofVirtual().start(this::removeStaleResultSets);
      }
    }
    return id;
  }

  void remove(QueryId id) {
    synchronized (cache) {
      LOG.trace("Removing query (id={}) from cache", id);
      ResultSetInfo info = cache.remove(id);
      if (info != null) {
        close(id, info);
        if (cache.isEmpty() && cleaner != null) {
          cleaner.interrupt();
          cleaner = null;
        }
      }
    }
  }

  private void removeStaleResultSets() {
    try {
      Thread.sleep(CHECK_INTERVAL);
      synchronized (cache) {
        final long now = Instant.now().getEpochSecond();
        // Copy keys to avoid ConcurrentModificationException
        List<QueryId> ids = List.copyOf(cache.keySet());
        for (QueryId id : ids) {
          ResultSetInfo info = cache.get(id);
          if (now - info.lastRequested > info.stayAliveSeconds) {
            LOG.trace("Evicting stale query (id={}) from cache", id);
            close(id, info);
            cache.remove(id);
          }
        }
        if (cache.isEmpty() && cleaner != null) {
          cleaner.interrupt();
          cleaner = null;
        }
      }
    } catch (InterruptedException e) {
      LOG.trace("Aborting staleness check");
    }
  }

  void clearCache() {
    synchronized (cache) {
      cleaner.interrupt();
      cleaner = null;
      try {
        cache.forEach(this::close);
      } finally {
        cache.clear();
      }
    }
  }

  private void close(QueryId id, ResultSetInfo info) {
    try {
      info.rs.close();
      if (info.closeConnection) {
        LOG.trace("Closing connection associated with query (id={})", id);
        info.con.close();
      }
    } catch (SQLException e) {
      LOG.error(e.toString(), e);
    }
  }


}
