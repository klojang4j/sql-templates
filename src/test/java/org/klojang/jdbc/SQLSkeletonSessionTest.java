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

import static org.junit.jupiter.api.Assertions.assertFalse;

public class SQLSkeletonSessionTest {

  private static final String DB_DIR = System.getProperty("user.home") + "/klojang-jdbc-tests/SQLSkeletonSessionTest/h2";
  private static final ThreadLocal<Connection> MY_CON = new ThreadLocal<>();

  public record Person(Integer id, String firstName, String lastName, int age) { }

  @BeforeEach
  public void before() throws IOException, SQLException {
    IOMethods.rm(DB_DIR);
    Files.createDirectories(Path.of(DB_DIR));
    Connection c = DriverManager.getConnection("jdbc:h2:" + DB_DIR + "/test");
    MY_CON.set(c);
  }

  @AfterEach
  public void after() throws SQLException {
    afterAll();
  }

  @AfterAll
  public static void afterAll() {
    if (MY_CON.get() != null) {
      try {
        MY_CON.get().close();
      } catch (SQLException e) {
        // ...
      }
      MY_CON.set(null);
      IOMethods.rm(DB_DIR);
    }
  }

  @Test
  public void setIdentifier00() {
    Connection con = MY_CON.get();
    try {
      SQL.simpleUpdate(con, "CREATE TABLE \"PERSON DATA\" (NAME VARCHAR(255))").execute();
      SQLSession session = SQL.skeleton("SELECT * FROM ~%table%").session(MY_CON.get());
      session.setIdentifier("table", "PERSON DATA");
      try (SQLQuery query = session.prepareQuery()) {
        assertFalse(query.exists());
      }
    } finally {
      SQL.simpleUpdate(con, "DROP TABLE \"PERSON DATA\"").execute();
    }
  }


  @Test
  public void setNestedIdentifier01() {
    Connection con = MY_CON.get();
    try {
      SQL.simpleUpdate(con, "CREATE TABLE \"PERSON DATA\" (NAME VARCHAR(255))").execute();
      // Ridiculous way to do it, but hey - it's a test
      String sql = "SELECT * FROM ~%%begin:from% ~%table% ~%%end:from%";
      SQLSession session = SQL.skeleton(sql).session(MY_CON.get());
      session.setNestedIdentifier("from.table", "PERSON DATA");
      try (SQLQuery query = session.prepareQuery()) {
        assertFalse(query.exists());
      }
    } finally {
      SQL.simpleUpdate(con, "DROP TABLE \"PERSON DATA\"").execute();
    }
  }


}
