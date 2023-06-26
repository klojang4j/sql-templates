package org.klojang.jdbc;

import org.klojang.jdbc.x.sql.AbstractSQL;
import org.klojang.jdbc.x.sql.DynamicSQL;
import org.klojang.jdbc.x.sql.ParametrizedSQL;
import org.klojang.jdbc.x.sql.SQLTemplate;

import java.sql.Connection;

public sealed interface SQL permits AbstractSQL {

  static SQL parametrized(String sql) {
    return parametrized(sql, new BindInfo() {});
  }

  static SQL parametrized(String sql, BindInfo bindInfo) {
    return new ParametrizedSQL(sql, bindInfo);
  }

  static SQL template(String sql) {
    return template(sql, new BindInfo() {});
  }

  static SQL template(String sql, BindInfo bindInfo) {
    return new SQLTemplate(sql, bindInfo);
  }

  static SQL dynamic(String sql) {
    return dynamic(sql, new BindInfo() {});
  }

  static SQL dynamic(String sql, BindInfo bindInfo) {
    return new DynamicSQL(sql, bindInfo);
  }

  static SQLInsertBuilder prepareInsert() {
    return new SQLInsertBuilder();
  }

  SQL set(String varName, Object value);

  default SQL setSortColumn(Object sortColumn) {
    return set("sortColumn", sortColumn);
  }

  default SQL setSortOrder(Object sortOrder) {
    return (sortOrder instanceof Boolean)
          ? setDescending((Boolean) sortOrder)
          : set("sortOrder", sortOrder);
  }

  default SQL setDescending(boolean isDescending) {
    return set("sortOrder", isDescending ? "DESC" : "ASC");
  }

  default SQL setOrderBy(Object sortColumn, Object sortOrder) {
    return setSortColumn(sortColumn).setSortOrder(sortOrder);
  }

  default SQL setOrderBy(Object sortColumn, boolean isDescending) {
    return setSortColumn(sortColumn).setDescending(isDescending);
  }

  SQLQuery prepareQuery(Connection con);

  SQLInsert prepareInsert(Connection con);

  SQLUpdate prepareUpdate(Connection con);
}
