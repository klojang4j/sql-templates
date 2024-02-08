package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.invoke.BeanReader;
import org.klojang.jdbc.x.JDBC;
import org.klojang.jdbc.x.Utils;
import org.klojang.jdbc.x.sql.ParamExtractor;
import org.klojang.jdbc.x.sql.SQLInfo;
import org.klojang.templates.RenderSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static org.klojang.check.CommonChecks.*;
import static org.klojang.check.Tag.VARARGS;
import static org.klojang.jdbc.x.Strings.*;

final class SQLSkeletonSession extends DynamicSQLSession {

  private static final Logger LOG = LoggerFactory.getLogger(SQLSkeletonSession.class);

  SQLSkeletonSession(Connection con, AbstractSQL sql, RenderSession session) {
    super(con, sql, session);
  }

  @Override
  @SafeVarargs
  @SuppressWarnings({"unchecked"})
  public final <T> SQLSession setValues(T... beans) {
    Check.notNull(beans, VARARGS).is(deepNotNull()).isNot(empty());
    Class<T> clazz = (Class<T>) beans.getClass().getComponentType();
    BeanReader<T> reader = new BeanReader<>(clazz);
    return setValues(Arrays.asList(beans), reader, BeanValueProcessor.identity());
  }

  @Override
  public <T> SQLSession setValues(List<T> beans) {
    return setValues(beans, BeanValueProcessor.identity());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> SQLSession setValues(List<T> beans, BeanValueProcessor<T> processor) {
    Check.that(beans, BEANS).is(deepNotNull()).isNot(empty());
    Check.notNull(processor, PROCESSOR);
    Class<T> clazz = (Class<T>) beans.getFirst().getClass();
    BeanReader<T> reader = new BeanReader<>(clazz);
    return setValues(beans, reader, processor);
  }

  @Override
  public <T> long[] setValuesAndExecute(List<T> beans) {
    return setValuesAndExecute(beans, BeanValueProcessor.identity());
  }

  @Override
  public <T> long[] setValuesAndExecute(List<T> beans, BeanValueProcessor<T> processor) {
    String executable = sql.unparsed();
    try (Statement stmt = con.createStatement()) {
      setValues(beans, processor);
      Check.that(session).isNot(RenderSession::hasUnsetVariables, rogueVariables());
      executable = session.render();
      LOG.trace(EXECUTING_SQL, executable);
      stmt.executeUpdate(executable, RETURN_GENERATED_KEYS);
      return JDBC.getGeneratedKeys(stmt, beans.size());
    } catch (SQLException e) {
      throw Utils.wrap(e, executable);
    } finally {
      close();
    }
  }

  private <T> SQLSession setValues(List<T> beans,
        BeanReader<T> reader,
        BeanValueProcessor<T> processor) {
    if (!session.getTemplate().hasNestedTemplate(RECORD)) {
      throw new DatabaseException("missing nested template \"record\"");
    }
    String[] vars = session.getTemplate()
          .getNestedTemplate(RECORD)
          .getVariables()
          .toArray(String[]::new);
    List<Map<String, String>> quoted = new ArrayList<>(beans.size());
    Quoter quoter = new Quoter(statement());
    for (T bean : beans) {
      Map<String, String> map = HashMap.newHashMap(vars.length);
      for (String var : vars) {
        Object in = reader.read(bean, var);
        String out = quoteValue(processor.process(bean, var, in, quoter));
        map.put(var, out);
      }
      quoted.add(map);
    }
    session.populate(RECORD, quoted, ",");
    return this;
  }

  @Override
  public SQLQuery prepareQuery() {
    close();
    SQLInfo sqlInfo = getSQLInfo();
    PreparedStatement ps = JDBC.getPreparedStatement(con, sqlInfo);
    return new SQLQuery(ps, this, sqlInfo);
  }

  @Override
  public SQLInsert prepareInsert(boolean retrieveKeys) {
    close();
    SQLInfo sqlInfo = getSQLInfo();
    PreparedStatement ps = JDBC.getPreparedStatement(con, sqlInfo, retrieveKeys);
    return new SQLInsert(ps, this, sqlInfo, retrieveKeys);
  }

  @Override
  public SQLUpdate prepareUpdate() {
    close();
    SQLInfo sqlInfo = getSQLInfo();
    PreparedStatement ps = JDBC.getPreparedStatement(con, sqlInfo);
    return new SQLUpdate(ps, this, sqlInfo);
  }

  private SQLInfo getSQLInfo() {
    Check.that(session.hasUnsetVariables()).is(no(), rogueVariables());
    return new SQLInfo(new ParamExtractor(session.render()));
  }


}
