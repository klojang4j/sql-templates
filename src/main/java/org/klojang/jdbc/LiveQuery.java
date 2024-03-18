package org.klojang.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.time.Instant;

import static org.klojang.jdbc.BatchQuery.QueryId;
import static org.klojang.jdbc.x.Utils.CENTRAL_CLEANER;

final class LiveQuery {

  private static final Logger LOG = LoggerFactory.getLogger(LiveQuery.class);

  private final QueryContainer query;
  private final long stayAliveSeconds;

  long lastRequested;

  LiveQuery(SQLQuery query,
        long stayAliveSeconds,
        boolean closeConnection) {
    this.query = new QueryContainer(query, closeConnection);
    this.stayAliveSeconds = stayAliveSeconds;
    this.lastRequested = Instant.now().getEpochSecond();
    CENTRAL_CLEANER.register(this, this.query);
  }

  boolean isStale() {
    long now = Instant.now().getEpochSecond();
    return now - lastRequested > stayAliveSeconds;
  }

  ResultSet getResultSet() {
    lastRequested = Instant.now().getEpochSecond();
    return query.get().getResultSet();
  }

  SQLQuery getSQLQuery() {
    return query.get();
  }

  void terminate(QueryId id) {
    query.terminate(id);
  }


  private static class QueryContainer implements Runnable {

    private final SQLQuery query;
    private final boolean closeConnection;

    QueryContainer(SQLQuery query, boolean closeConnection) {
      this.query = query;
      this.closeConnection = closeConnection;
    }

    @Override
    public void run() {
      try {
        query.close();
        if (closeConnection) {
          query.getSession().getConnection().close();
        }
      } catch (Throwable t) {
        // ...
      }
    }

    SQLQuery get() { return query; }

    void terminate(QueryId id) {
      LOG.trace("Terminating query (id={})", id);
      try {
        query.close();
        if (closeConnection) {
          LOG.trace("Closing connection for query (id={})", id);
          query.getSession().getConnection().close();
        }
      } catch (Throwable t) {
        LOG.error(t.toString(), t);
      }
    }

  }
}
