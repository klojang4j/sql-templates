package org.klojang.jdbc.x.sql;

import org.klojang.jdbc.BindInfo;
import org.klojang.jdbc.SQLSession;

public final class ParametrizedSQL extends AbstractSQL {

    private final SQLInfo sqlInfo;

    public ParametrizedSQL(String sql, BindInfo bindInfo) {
        super(sql, bindInfo);
        sqlInfo = new SQLInfo(new SQLNormalizer(sql));
    }

    public SQLSession newSession() {
        return new ParametrizedSQLSession(this, sqlInfo);
    }

}
