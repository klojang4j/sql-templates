/**
 * <i>Klojang JDBC</i> is a thin, transparent abstraction layer around standard JDBC. It
 * builds on
 * <a href="https://klojang4j.github.io/klojang-templates/1/api">Klojang Templates</a> to
 * provide functionality not commonly found in JDBC extension libraries. It provides the
 * following functionality:
 *
 * <ol>
 *   <li>Convert {@link java.sql.ResultSet ResultSet} rows into JavaBeans or
 *   {@code Map<String, Object>} pseudo-objects. There is not pretense at all of providing
 *   full-fledged ORM functionality. The rows are converted to "flat" beans or maps. The
 *   type of the JavaBean may have additional structure, but only top-level properties are
 *   populated.
 *   <li>Enable parametrization of parts of SQL that cannot be parametrized using
 *   {@linkplain java.sql.PreparedStatement prepared statements} alone.
 *   <i>Klojang JDBC</i> allows you to do this without exposing yourself to the
 *   dangers of SQL Injection.
 *   <li>Special attention has been paid to persisting Java objects in potentially very
 *   large batches.
 * </ol>
 */
module org.klojang.db {

  exports org.klojang.jdbc;

  requires java.sql;

  requires org.slf4j;

  requires org.klojang.check;
  requires org.klojang.util;
  requires org.klojang.collections;
  requires org.klojang.convert;
  requires org.klojang.invoke;
  requires org.klojang.templates;
}
