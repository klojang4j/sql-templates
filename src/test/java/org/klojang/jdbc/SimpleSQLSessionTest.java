package org.klojang.jdbc;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.klojang.util.IOMethods;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleSQLSessionTest {
  private static final String DB_DIR = System.getProperty("user.home") + "/h2.SimpleSQLSessionTest";
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
  public void staticSQL00() {
    Connection con = MY_CON.get();
    SQL.staticSQL("CREATE LOCAL TEMPORARY TABLE FOO(BAR VARCHAR(255))")
          .session(con)
          .execute();
    SQL.staticSQL("INSERT INTO FOO(BAR) VALUES('BOZO')").session(con).execute();
    String s = SQL.staticSQL("SELECT BAR FROM FOO")
          .session(con)
          .prepareQuery()
          .getString()
          .get();
    assertEquals("BOZO", s);
    SQL.staticSQL("DROP TABLE FOO").session(con).execute();
  }

  @Test
  public void staticSQL01() {
    Connection con = MY_CON.get();
    SQL.staticSQL("CREATE LOCAL TEMPORARY TABLE FOO(BAR VARCHAR(255))")
          .session(con)
          .prepareUpdate()
          .execute();
    SQL.staticSQL("INSERT INTO FOO(BAR) VALUES('BOZO')")
          .session(con)
          .prepareUpdate()
          .execute();
    String s = SQL.staticSQL("SELECT BAR FROM FOO")
          .session(con)
          .prepareQuery()
          .getString()
          .get();
    assertEquals("BOZO", s);
    SQL.staticSQL("DROP TABLE FOO").session(con).prepareUpdate().execute();
  }
}
