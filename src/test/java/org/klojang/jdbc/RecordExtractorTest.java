package org.klojang.jdbc;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.klojang.templates.name.CamelCaseToSnakeUpperCase;
import org.klojang.util.IOMethods;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class RecordExtractorTest {
  private static final String DB_DIR = System.getProperty("user.home") + "/klojang-jdbc-tests/RecordExtractorTest";
  private static final ThreadLocal<Connection> MY_CON = new ThreadLocal<>();

  public record Person(
        int personId,
        String firstName,
        String lastName,
        LocalDate birthDate) {

    public String toString() {
      return firstName + " " + lastName + " (" + birthDate + ")";
    }

  }

  @BeforeEach
  public void before() throws IOException, SQLException {
    IOMethods.rm(DB_DIR);
    Files.createDirectories(Path.of(DB_DIR));
    Connection con = DriverManager.getConnection("jdbc:h2:" + DB_DIR + "/test");
    MY_CON.set(con);
    String sql = """
          CREATE TABLE PERSON(
            PERSON_ID INT AUTO_INCREMENT, 
            FIRST_NAME VARCHAR(255),
            LAST_NAME VARCHAR(255),
            BIRTH_DATE DATE)
          """;
    SQL.simple(sql).session(con).prepareUpdate().execute();
    List<Person> persons = List.of(
          new Person(0, "John", "Smith", LocalDate.of(1960, 4, 15)),
          new Person(0, "Mary", "Smith", LocalDate.of(1980, 10, 5)),
          new Person(0, "Joan", "de Santos", LocalDate.of(1977, 5, 23)),
          new Person(0, "Jill", "Kriel", LocalDate.of(1977, 2, 10)),
          new Person(0, "Stephen", "Bester", LocalDate.of(2001, 2, 8)),
          new Person(0, "Carlos", "Smith", LocalDate.of(2004, 2, 8)),
          new Person(0, "Mary", "Bear", LocalDate.of(1956, 11, 7)),
          new Person(0, "Dieter", "Washington", LocalDate.of(1989, 2, 4)),
          new Person(0, "Peter", "Peterson", LocalDate.of(1963, 5, 3)),
          new Person(0, "Joe", "Peterson", LocalDate.of(1998, 9, 23))
    );
    SQLBatchInsert<Person> bi = SQL.insertBatch()
          .of(Person.class)
          .into("PERSON")
          .excluding("personId")
          .withNameMapper(new CamelCaseToSnakeUpperCase())
          .prepare(con);
    bi.insertBatch(persons);
   }

  @AfterEach
  public void after() throws SQLException, IOException {
    if (MY_CON.get() != null) {
      MY_CON.get().close();
    }
    IOMethods.rm(DB_DIR);
  }

  @AfterAll
  public static void afterAll() throws SQLException, IOException {
    if (MY_CON.get() != null) {
      MY_CON.get().close();
    }
    IOMethods.rm(DB_DIR);
  }

  @Test
  public void test00() {
    String sql = """
          SELECT * FROM PERSON
           WHERE LAST_NAME = :lastName
           ORDER BY ~%column% ~%direction%
          """;
    SQLSession session = SQL.template(sql).session(MY_CON.get());
    session.set("column", "LAST_NAME").set("direction", "DESC");
    try (SQLQuery query = session.prepareQuery()) {
      List<Person> persons = query
            .bind("lastName", "Smith")
            .getExtractor(Person.class)
            .extractAll();
      for (Person person : persons) {
        System.out.println(person);
      }
    }
  }

}
