package org.klojang.jdbc.x;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.klojang.jdbc.SQL;
import org.klojang.util.IOMethods;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JDBCTest {
  private static final String DB_DIR = System.getProperty("user.home") + "/klojang-jdbc-tests/JDBCTest/h2";
  private static final ThreadLocal<Connection> MY_CON = new ThreadLocal<>();

  @BeforeEach
  public void before() throws IOException, SQLException {
    IOMethods.rm(DB_DIR);
    Files.createDirectories(Path.of(DB_DIR));
    Connection c = DriverManager.getConnection("jdbc:h2:" + DB_DIR + "/test");
    MY_CON.set(c);
  }

  @AfterEach
  public void after() {
    afterAll();
  }

  @AfterAll
  public static void afterAll() {
    if (MY_CON.get() != null) {
      try {
        MY_CON.get().close();
      } catch (Throwable t) {
        // ...
      } finally {
        MY_CON.set(null);
      }
    }
    IOMethods.rm(DB_DIR);
  }

  @Test
  public void getGeneratedKeys00() throws SQLException {
    String sql = "CREATE LOCAL TEMPORARY TABLE EMP(ID INT, NAME VARCHAR(255))";
    SQL.staticSQL(sql).session(MY_CON.get()).execute();
    sql = "INSERT INTO EMP(ID,NAME)VALUES(1,'Foo')";
    PreparedStatement stmt = MY_CON.get()
          .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    stmt.execute();
    long[] ids = JDBC.getGeneratedKeys(stmt, 1);
    assertEquals(0, ids.length);
  }


}
