/**
 * <p><i>Klojang JDBC</i> is a thin, non-intrusive abstraction layer around standard JDBC
 * that builds on
 * <a href="https://klojang4j.github.io/klojang-templates/1/api">Klojang Templates</a> to
 * provide functionality not commonly found in JDBC extension libraries. These are its
 * main features:
 *
 * <ol>
 *   <li>Convert {@link java.sql.ResultSet ResultSet} rows into JavaBeans, records, or
 *       {@code Map<String, Object>} pseudo-objects. There is not pretense here of
 *       providing full-fledged ORM functionality. The rows are converted to "flat" beans,
 *       records, or maps. The type of the JavaBean may have additional structure, but
 *       only top-level properties are populated.
 *   <li>Enable parametrization of parts of SQL that cannot be parametrized using
 *       {@linkplain java.sql.PreparedStatement prepared statements} alone.
 *       <i>Klojang JDBC</i> lets you to do this without exposing yourself to the
 *       dangers of SQL Injection. Values will still be escaped and quoted according to
 *       the requirements of the target database.
 *   <li>Special attention has been paid to persisting Java objects in large batches.
 * </ol>
 *
 * <p>Here is an example that goes full-circle from CREATE TABLE, via INSERT to SELECT:
 *
 * <blockquote><pre>{@code
 * Connection con = ... // get or create JDBC Connection
 *
 * String sql = """
 *      CREATE TABLE PERSON(
 *          PERSON_ID INT AUTO_INCREMENT,
 *          FIRST_NAME VARCHAR(255),
 *          LAST_NAME VARCHAR(255),
 *          BIRTH_DATE DATE)
 *      """;
 * SQL.staticSQL(sql).session(con).execute();
 *
 * List<Person> persons = List.of(
 *    new Person("John", "Smith", LocalDate.of(1960, 4, 15)),
 *    new Person("Mary", "Smith", LocalDate.of(1980, 10, 5)),
 *    new Person("Joan", "de Santos", LocalDate.of(1977, 5, 23)),
 *    new Person("Jill", "Jackson", LocalDate.of(1977, 2, 10)),
 *    new Person("Mary", "Bear", LocalDate.of(1956, 11, 7)),
 *    new Person("Joe", "Peterson", LocalDate.of(1998, 9, 23))
 * );
 *
 * SQLBatchInsert batchInsert = SQL.insertBatch()
 *    .of(Person.class)
 *    .excluding("personId")
 *    .prepare(con);
 * // Save the beans and set their personId property to the database-generated key
 * batchInsert.insertBatchAndSetIDs("personId", persons);
 *
 * sql = "SELECT * FROM PERSON WHERE LAST_NAME = :lastName ORDER BY ~%orderBy%";
 * SQLSession session = SQL.template(sql).session(con);
 * session.setOrderBy("SALARY");
 * try (SQLQuery query = session.prepareQuery()) {
 *   query.bind("lastName", "Smith");
 *   BeanExtractor<Person> extractor = query.getExtractor(Person.class);
 *   List<Person> persons = extractor.extractAll();
 *   persons.forEach(System.out::print);
 * }
 * }</pre></blockquote>
 *
 * @see org.klojang.templates.name.SnakeCaseToCamelCase
 * @see org.klojang.templates.name.CamelCaseToSnakeUpperCase
 */
module org.klojang.jdbc {

  exports org.klojang.jdbc;
  exports org.klojang.jdbc.util;

  requires java.sql;

  requires org.slf4j;

  requires org.klojang.check;
  requires org.klojang.util;
  requires org.klojang.collections;
  requires org.klojang.convert;
  requires org.klojang.invoke;
  requires org.klojang.templates;
}
