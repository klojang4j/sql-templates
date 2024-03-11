package org.klojang.jdbc;

import org.klojang.jdbc.x.JDBC;
import org.klojang.jdbc.x.sql.ParamExtractor;
import org.klojang.jdbc.x.sql.ParameterInfo;
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
    var paramInfo = getParamInfo(session, extractor);
    var stmt = JDBC.getPreparedStatement(con, paramInfo);
    return new SQLQuery(stmt, this, paramInfo);
  }

  public SQLInsert prepareInsert(boolean retrieveKeys) {
    close();
    var paramInfo = getParamInfo(session, extractor);
    var stmt = JDBC.getPreparedStatement(con, paramInfo, retrieveKeys);
    return new SQLInsert(stmt, this, paramInfo, retrieveKeys);
  }

  public SQLUpdate prepareUpdate() {
    close();
    var paramInfo = getParamInfo(session, extractor);
    var stmt = JDBC.getPreparedStatement(con, paramInfo);
    return new SQLUpdate(stmt, this, paramInfo);
  }

  private ParameterInfo getParamInfo(RenderSession session, ParamExtractor extractor) {
    return new ParameterInfo(session.render(), extractor);
  }

}
