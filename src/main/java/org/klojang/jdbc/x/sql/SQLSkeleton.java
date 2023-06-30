package org.klojang.jdbc.x.sql;

import org.klojang.jdbc.BindInfo;
import org.klojang.jdbc.KlojangSQLException;
import org.klojang.jdbc.SQLSession;
import org.klojang.templates.ParseException;
import org.klojang.templates.Template;

public final class SQLSkeleton extends AbstractSQL {

    private final Template template;

    public SQLSkeleton(String sql, BindInfo bindInfo) {
        super(sql, bindInfo);
        try {
            template = Template.fromString(sql);
        } catch (ParseException e) {
            throw new KlojangSQLException(e);
        }
    }

    public SQLSession newSession() {
        return new SQLSkeletonSession(this, template.newRenderSession());
    }

}
