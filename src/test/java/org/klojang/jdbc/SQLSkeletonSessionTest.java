package org.klojang.jdbc;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.klojang.util.ArrayMethods;
import org.klojang.util.IOMethods;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SQLSkeletonSessionTest {

  private static final String DB_DIR = System.getProperty("user.home") + "/h2.SQLSkeletonSessionTest";
  private static final ThreadLocal<Connection> MY_CON = new ThreadLocal<>();

  public record Person(Integer id, String firstName, String lastName, int age) { }

  @BeforeEach
  public void before() throws IOException, SQLException {
    IOMethods.rm(DB_DIR);
    Files.createDirectories(Path.of(DB_DIR));
    Connection c = DriverManager.getConnection("jdbc:h2:" + DB_DIR + "/test");
    String sql = """
          CREATE LOCAL TEMPORARY TABLE PERSON(
            ID INT AUTO_INCREMENT, 
            FIRST_NAME VARCHAR(255),
            LAST_NAME VARCHAR(255),
            AGE INT)
          """;
    try (Statement stmt = c.createStatement()) {
      stmt.executeUpdate(sql);
    }
    MY_CON.set(c);
  }

  @AfterEach
  public void after() throws SQLException {
    if (MY_CON.get() != null) {
      MY_CON.get().close();
    }
    IOMethods.rm(DB_DIR);
  }

  @AfterAll
  public static void afterAll() {
    if (MY_CON.get() != null) {
      try {
        MY_CON.get().close();
        IOMethods.rm(DB_DIR);
      } catch (SQLException e) {
        // ...
      }
    }
  }

  @Test
  public void setValues00() throws Exception {
    List<Person> persons = List.of(
          new Person(null, "John", "Smith", 34),
          new Person(null, "Francis", "O'Donell", 27),
          new Person(null, "Mary", "Bear", 52));
    // ...
    SQL sql = SQL.skeleton("""
          INSERT INTO PERSON(FIRST_NAME,LAST_NAME,AGE) VALUES
          ~%%begin:record%
          (~%firstName%,~%lastName%,~%age%)
          ~%%end:record%
          """);
    try (Connection conn = MY_CON.get()) {
      SQLSession session = sql.session(conn);
      session.setValues(persons).execute();

      int i = SQL.simpleQuery(MY_CON.get(), "SELECT COUNT(*) FROM PERSON")
            .getInt()
            .get();
      assertEquals(3, i);
    }
  }

  @Test
  public void setValues01() throws Exception {
    List<Person> persons = List.of(
          new Person(null, "John", "Smith", 34),
          new Person(null, "Francis", "O'Donell", 27),
          new Person(null, "Mary", "Bear", 52));
    // ...
    SQL sql = SQL.skeleton("""
          INSERT INTO PERSON(FIRST_NAME,LAST_NAME,AGE) VALUES
          ~%%begin:foo_bar%
          (~%firstName%,~%lastName%,~%age%)
          ~%%end:foo_bar%
          """);
    try (Connection conn = MY_CON.get()) {
      SQLSession session = sql.session(conn);
      // missing nested template "records"
      assertThrows(DatabaseException.class, () -> session.setValues(persons));
    }

  }


  @Test
  public void setValues02() throws Exception {
    List<Person> persons = List.of(
          new Person(null, "John", "Smith", 34),
          new Person(null, "Francis", "O'Donell", 27),
          new Person(null, "Mary", "Bear", 52));
    SQL sql = SQL.skeleton("""
          INSERT INTO PERSON(FIRST_NAME,LAST_NAME,AGE) VALUES
          ~%%begin:record%
          (~%firstName%,~%lastName%,~%age%)
          ~%%end:record%
          """);
    BeanValueProcessor<Person> processor = (bean, prop, val, quoter) -> {
      if (prop.equals("firstName")) {
        return quoter.sqlFunction("SUBSTRING", val, 1, 3);
      }
      return val;
    };
    try (Connection conn = MY_CON.get()) {
      SQLSession session = sql.session(conn);
      session.setValues(persons, processor).execute();
      String query = "SELECT FIRST_NAME FROM PERSON";
      List<String> firstNames = SQL.simpleQuery(MY_CON.get(), query).firstColumn();
      assertEquals(List.of("Joh", "Fra", "Mar"), firstNames);
    }
  }

  @Test
  public void setValuesAndExecute00() throws Exception {
    List<Person> persons = List.of(
          new Person(null, "John", "Smith", 34),
          new Person(null, "Francis", "O'Donell", 27),
          new Person(null, "Mary", "Bear", 52));
    SQL sql = SQL.skeleton("""
          INSERT INTO PERSON(FIRST_NAME,LAST_NAME,AGE) VALUES
          ~%%begin:record%
          (~%firstName%,~%lastName%,~%age%)
          ~%%end:record%
          """);
    try (Connection conn = MY_CON.get()) {
      SQLSession session = sql.session(conn);
      List<Long> ids0 = ArrayMethods.asList(session.setValuesAndExecute(persons));
      String query = "SELECT ID FROM PERSON";
      List<Long> ids1 = SQL.simpleQuery(MY_CON.get(), query).firstColumn(Long.class);
      assertEquals(ids0, ids1);
    }
  }


}
