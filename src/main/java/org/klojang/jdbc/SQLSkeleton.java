package org.klojang.jdbc;

import org.klojang.jdbc.x.Utils;
import org.klojang.templates.ParseException;
import org.klojang.templates.Template;

import java.sql.Connection;

final class SQLSkeleton extends AbstractSQL {

  private final Template template;

  SQLSkeleton(String sql, BindInfo bindInfo) {
    super(sql, bindInfo);
    try {
      template = Template.fromString(sql);
    } catch (ParseException e) {
      throw Utils.wrap(e);
    }
  }

  @Override
  public SQLSession session(Connection con) {
    return new SQLSkeletonSession(con, this, template.newRenderSession());
  }

}
