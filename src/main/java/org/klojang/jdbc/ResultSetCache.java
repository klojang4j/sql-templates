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
import static org.klojang.jdbc.x.Utils.CENTRAL_CLEANER;

final class ResultSetCache {

  private static final Logger LOG = LoggerFactory.getLogger(ResultSetCache.class);
  private static final long CHECK_INTERVAL = 2 * 60 * 1000; // every 2 minutes

  private static final ResultSetCache instance = new ResultSetCache();


  private final State state;

  private ResultSetCache() {
    state = new State();
    CENTRAL_CLEANER.register(this, state);
  }

  static ResultSetCache getInstance() { return instance; }

  ResultSet get(QueryId id) {
    synchronized (cache()) {
      ResultSetInfo info = state.cache.get(id);
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
    synchronized (cache()) {
      Utils.check(id).isNot(keyIn(), cache(), "query already registered (id={})", id);
      LOG.trace("Registering query (id={})", id);
      cache().put(id, new ResultSetInfo(con, rs, stayAliveSeconds, closeConnection));
      if (cleaner() == null) {
        state.cleaner = Thread.ofVirtual().start(this::removeStaleResultSets);
      }
    }
    return id;
  }

  void remove(QueryId id) {
    synchronized (cache()) {
      LOG.trace("Removing query (id={}) from cache", id);
      ResultSetInfo info = cache().remove(id);
      if (info != null) {
        info.close(id);
        if (cache().isEmpty() && cleaner() != null) {
          cleaner().interrupt();
          state.cleaner = null;
        }
      }
    }
  }

  private void removeStaleResultSets() {
    try {
      while (true) {
        Thread.sleep(CHECK_INTERVAL);
        synchronized (cache()) {
          final long now = Instant.now().getEpochSecond();
          // Copy keys to avoid ConcurrentModificationException
          List<QueryId> ids = List.copyOf(cache().keySet());
          for (QueryId id : ids) {
            ResultSetInfo info = cache().get(id);
            if (now - info.lastRequested > info.stayAliveSeconds) {
              LOG.trace("Evicting stale query (id={}) from cache", id);
              info.close(id);
              cache().remove(id);
            }
          }
          if (cache().isEmpty()) {
            break;
          }
        }
      }
    } catch (InterruptedException e) {
      LOG.trace("Aborting staleness check");
    } finally {
      state.cleaner = null;
    }
  }

  void clearCache() {
    if (cleaner() != null) {
      cleaner().interrupt();
      state.cleaner = null;
    }
    synchronized (cache()) {
      LOG.trace("Terminating all queries");
      cache().forEach((id, info) -> info.close(id));
      cache().clear();
    }
  }

  private Map<QueryId, ResultSetInfo> cache() { return state.cache; }

  private Thread cleaner() { return state.cleaner; }


  private static class State implements Runnable {
    private final Map<QueryId, ResultSetInfo> cache = new HashMap<>();
    private Thread cleaner;

    @Override
    public void run() {
      try {
        if (cleaner != null) {
          cleaner.interrupt();
        }
      } finally {
        try {
          cache.values().forEach(ResultSetInfo::close);
        } finally {
          cache.clear();
        }
      }
    }
  }

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

    void close(QueryId id) {
      LOG.trace("Closing connection associated with query (id={})", id);
      try {
        rs.close();
        if (closeConnection) {
          con.close();
        }
      } catch (SQLException e) {
        LOG.error(e.toString(), e);
      }
    }

    void close() {
      try {
        rs.close();
        if (closeConnection) {
          con.close();
        }
      } catch (SQLException e) {
        // ...
      }
    }
  }


}
