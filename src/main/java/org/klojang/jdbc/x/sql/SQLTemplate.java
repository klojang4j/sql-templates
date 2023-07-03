package org.klojang.jdbc.x.sql;

import org.klojang.jdbc.BindInfo;
import org.klojang.jdbc.KlojangSQLException;
import org.klojang.jdbc.SQLSession;
import org.klojang.templates.ParseException;
import org.klojang.templates.Template;

public final class SQLTemplate extends AbstractSQL {


    private final Template template;
    private final SQLNormalizer normalizer;

    public SQLTemplate(String sql, BindInfo bindInfo) {
        super(sql, bindInfo);
        normalizer = new SQLNormalizer(sql);
        try {
            template = Template.fromString(normalizer.getNormalizedSQL());
        } catch (ParseException e) {
            throw new KlojangSQLException(e);
        }
    }

    public SQLSession session() {
        return new SQLTemplateSession(this, normalizer, template.newRenderSession());
    }

}
