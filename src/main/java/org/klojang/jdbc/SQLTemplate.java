package org.klojang.jdbc;

import org.klojang.jdbc.x.sql.SQLNormalizer;
import org.klojang.templates.ParseException;
import org.klojang.templates.Template;

import java.sql.Connection;

final class SQLTemplate extends AbstractSQL {

  private final Template template;
  private final SQLNormalizer normalizer;

  SQLTemplate(String sql, BindInfo bindInfo) {
    super(sql, bindInfo);
    normalizer = new SQLNormalizer(sql);
    try {
      template = Template.fromString(normalizer.getNormalizedSQL());
    } catch (ParseException e) {
      throw new KlojangSQLException(e);
    }
  }

  @Override
  public SQLSession session(Connection con) {
    return new SQLTemplateSession(con, this, normalizer, template.newRenderSession());
  }

}
