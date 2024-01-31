package org.klojang.jdbc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.klojang.templates.name.CamelCaseToSnakeUpperCase;
import org.klojang.templates.name.SnakeCaseToCamelCase;
import org.klojang.util.IOMethods;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class SQLQueryTest {
  private static final String DB_DIR = System.getProperty("user.home") + "/klojang-db-query-test";
  private static final ThreadLocal<Connection> MY_CON = new ThreadLocal<>();

  public static class Person {
    int personId;
    String firstName;
    String lastName;
    LocalDate birthDate;

    public Person() { }

    public Person(int personId, String firstName, String lastName, LocalDate birthDate) {
      this.personId = personId;
      this.firstName = firstName;
      this.lastName = lastName;
      this.birthDate = birthDate;
    }

    public int getPersonId() {
      return personId;
    }

    public String getFirstName() {
      return firstName;
    }

    public String getLastName() {
      return lastName;
    }

    public LocalDate getBirthDate() {
      return birthDate;
    }

    public void setPersonId(int id) {
      this.personId = id;
    }

    public void setFirstName(String firstName) {
      this.firstName = firstName;
    }

    public void setLastName(String lastName) {
      this.lastName = lastName;
    }

    public void setBirthDate(LocalDate birthDate) {
      this.birthDate = birthDate;
    }

    public String toString() {
      return firstName + " " + lastName + " (" + birthDate + ")";
    }
  }

  @BeforeEach
  public void before() throws IOException, SQLException {
    IOMethods.rm(DB_DIR);
    Files.createDirectories(Path.of(DB_DIR));
    Connection con = DriverManager.getConnection("jdbc:h2:" + DB_DIR + "/test");
    String sql = """
          CREATE LOCAL TEMPORARY TABLE PERSON(
            ID INT AUTO_INCREMENT, 
            FIRST_NAME VARCHAR(255),
            LAST_NAME VARCHAR(255),
            BIRTH_DATE DATE)
          """;
    try (Statement stmt = con.createStatement()) {
      stmt.executeUpdate(sql);
    }
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
    SQLBatchInsert bi = SQL.configureBatchInsert()
          .of(Person.class)
          .into("PERSON")
          .excluding("personId")
          .withNameMapper(new CamelCaseToSnakeUpperCase())
          .prepare(con);
    bi.insertBatchAndSetIDs("personId", persons);
    MY_CON.set(con);
  }

  @AfterEach
  public void after() throws SQLException, IOException {
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
            .withNameMapper(new SnakeCaseToCamelCase())
            .bind("lastName", "Smith")
            .getBeanifier(Person.class)
            .beanifyAll();
      for (Person person : persons) {
        System.out.println(person);
      }
    }
  }

  @Test
  public void lookup00() throws Exception {
    String sql = """
          SELECT FALSE FROM PERSON
           LIMIT :limit
          """;
    try (SQLSession session = SQL.simple(sql).session(MY_CON.get())) {
      try (SQLQuery query = session.prepareQuery()) {
        boolean b = query.bind("limit", 1).lookup(boolean.class).get();
        assertFalse(b);
      }
    }
  }

  @Test
  public void firstColumn00() throws Exception {
    String sql = """
          SELECT ~%literal%, ~%limit% FROM PERSON
           LIMIT ~%limit%
          """;
    try (SQLSession session = SQL.skeleton(sql).session(MY_CON.get())) {
      session.setValue("literal", "O'Donell").setValue("limit", 2);
      try (SQLQuery query = session.prepareQuery()) {
        List<String> l = query.firstColumn();
        assertEquals(List.of("O'Donell", "O'Donell"), l);
      }
    }
  }

  @Test
  public void firstColumn01() throws Exception {
    String sql = """
          SELECT ~%column% FROM PERSON
           ORDER BY ~%sortColumn%
           LIMIT ~%limit%
          """;
    try (SQLSession session = SQL.skeleton(sql).session(MY_CON.get())) {
      session.setIdentifier("column", "LAST_NAME")
            .setOrderBy("LAST_NAME")
            .set("limit", 2);
      try (SQLQuery query = session.prepareQuery()) {
        List<String> l = query.firstColumn();
        assertEquals(List.of("Bear", "Bester"), l);
      }
    }
  }

}
