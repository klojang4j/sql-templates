package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.Utils;
import org.klojang.templates.ParseException;
import org.klojang.templates.Template;

import java.sql.Connection;

import static org.klojang.jdbc.x.Strings.CONNECTION;

final class SQLSkeleton extends AbstractSQL {

  private final Template template;

  SQLSkeleton(String sql, SessionConfig config) {
    super(sql, config);
    try {
      template = Template.fromString(sql);
    } catch (ParseException e) {
      throw Utils.wrap(e);
    }
  }

  @Override
  public SQLSession session(Connection con) {
    Check.notNull(con, CONNECTION);
    return new SQLSkeletonSession(con, this, template.newRenderSession());
  }

}
