package org.klojang.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.time.Instant;

import static org.klojang.jdbc.BatchQuery.QueryId;

final class LiveQuery {

  private static final Logger LOG = LoggerFactory.getLogger(LiveQuery.class);

  private final SQLQuery query;
  private final long stayAliveSeconds;
  private final boolean closeConnection;

  long lastRequested;

  LiveQuery(SQLQuery query,
        long stayAliveSeconds,
        boolean closeConnection) {
    this.query = query;
    this.stayAliveSeconds = stayAliveSeconds;
    this.closeConnection = closeConnection;
    this.lastRequested = Instant.now().getEpochSecond();
  }

  boolean isStale() {
    long now = Instant.now().getEpochSecond();
    return now - lastRequested > stayAliveSeconds;
  }

  ResultSet getResultSet() {
    lastRequested = Instant.now().getEpochSecond();
    return query.getResultSet();
  }

  void terminate(QueryId id) {
    LOG.trace("Terminating query (id={})", id);
    try {
      query.close();
      if (closeConnection) {
        LOG.trace("Closing connection for query (id={})", id);
        query.session.con.close();
      }
    } catch (Throwable t) {
      LOG.error(t.toString(), t);
    }
  }

  void kill() {
    try {
      query.close();
      if (closeConnection) {
        query.session.con.close();
      }
    } catch (Throwable t) {
      // ...
    }
  }
}
