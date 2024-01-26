package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.invoke.BeanReader;
import org.klojang.jdbc.x.JDBC;
import org.klojang.jdbc.x.sql.SQLInfo;
import org.klojang.jdbc.x.sql.SQLNormalizer;
import org.klojang.templates.RenderSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;

import static org.klojang.check.CommonChecks.empty;
import static org.klojang.check.Tag.VARARGS;

final class SQLSkeletonSession extends DynamicSQLSession {

  SQLSkeletonSession(Connection con, AbstractSQL sql, RenderSession session) {
    super(con, sql, session);
  }

  @SafeVarargs
  @SuppressWarnings({"unchecked"})
  public final <T> SQLSession setValues(T... beans) {
    Check.notNull(beans, VARARGS).isNot(empty());
    Class<T> beanType = (Class<T>) beans.getClass().getComponentType();
    return setValues0(new BeanReader<>(beanType), Arrays.asList(beans));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> SQLSession setValues(List<T> beans) {
    Check.that(beans, "beans").isNot(empty());
    Class<T> beanType = (Class<T>) beans.get(0).getClass();
    return setValues0(new BeanReader<>(beanType), beans);
  }

  @Override
  public <T> SQLSession setValues(BeanReader<T> reader, List<T> beans) {
    Check.notNull(reader, "reader");
    Check.that(beans, "beans").isNot(empty());
    return setValues0(reader, beans);
  }

  private <T> SQLSession setValues0(BeanReader<T> reader, List<T> beans) {
    if (!session.getTemplate().hasNestedTemplate("record")) {
      throw new KlojangSQLException("missing nested template \"record\"");
    }
    String[] vars = session.getTemplate()
          .getNestedTemplate("record")
          .getVariables()
          .toArray(String[]::new);
    List<Map<String, String>> quoted = new ArrayList<>(beans.size());
    for (T bean : beans) {
      Check.notNull(bean, "bean");
      Map<String, String> map = HashMap.newHashMap(vars.length);
      for (String var : vars) {
        map.put(var, quoteValue(reader.read(bean, var)));
      }
      quoted.add(map);
    }
    session.populate("record", quoted, ",");
    return this;
  }

  @Override
  public SQLQuery prepareQuery() {
    SQLInfo sqlInfo = getSQLInfo();
    PreparedStatement ps = JDBC.getPreparedStatement(con, sqlInfo);
    return new SQLQuery(ps, this, sqlInfo);
  }

  @Override
  public SQLInsert prepareInsert(boolean retrieveKeys) {
    SQLInfo sqlInfo = getSQLInfo();
    PreparedStatement ps = JDBC.getPreparedStatement(con, sqlInfo, retrieveKeys);
    return new SQLInsert(ps, this, sqlInfo, retrieveKeys);
  }

  @Override
  public SQLUpdate prepareUpdate() {
    SQLInfo sqlInfo = getSQLInfo();
    PreparedStatement ps = JDBC.getPreparedStatement(con, sqlInfo);
    return new SQLUpdate(ps, this, sqlInfo);
  }

  private SQLInfo getSQLInfo() {
    Check.that(session.getAllUnsetVariables()).is(empty(), sessionNotFinished(session));
    String sql = session.render();
    return new SQLInfo(sql, new SQLNormalizer(sql));
  }


}
