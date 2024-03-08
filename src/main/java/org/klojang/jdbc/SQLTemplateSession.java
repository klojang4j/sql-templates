package org.klojang.jdbc;

import org.klojang.jdbc.x.JDBC;
import org.klojang.jdbc.x.sql.ParamExtractor;
import org.klojang.jdbc.x.sql.SQLInfo;
import org.klojang.templates.RenderSession;

import java.sql.Connection;

final class SQLTemplateSession extends DynamicSQLSession {

  private final ParamExtractor extractor;

  SQLTemplateSession(Connection con,
        AbstractSQL sql,
        ParamExtractor extractor,
        RenderSession session) {
    super(con, sql, session);
    this.extractor = extractor;
  }

  public SQLQuery prepareQuery() {
    close();
    var sqlInfo = getSQLInfo(session, extractor);
    var ps = JDBC.getPreparedStatement(con, sqlInfo);
    return new SQLQuery(ps, this, sqlInfo);
  }

  public SQLInsert prepareInsert(boolean retrieveKeys) {
    close();
    var sqlInfo = getSQLInfo(session, extractor);
    var ps = JDBC.getPreparedStatement(con, sqlInfo, retrieveKeys);
    return new SQLInsert(ps, this, sqlInfo, retrieveKeys);
  }

  public SQLUpdate prepareUpdate() {
    close();
    var sqlInfo = getSQLInfo(session, extractor);
    var ps = JDBC.getPreparedStatement(con, sqlInfo);
    return new SQLUpdate(ps, this, sqlInfo);
  }

  private SQLInfo getSQLInfo(RenderSession session, ParamExtractor extractor) {
    return new SQLInfo(session.render(), extractor);
  }

}
