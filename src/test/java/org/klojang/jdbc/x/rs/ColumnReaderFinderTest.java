package org.klojang.jdbc.x.rs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.klojang.jdbc.SQL;
import org.klojang.jdbc.SQLSession;
import org.klojang.jdbc.Stuff;
import org.klojang.templates.name.SnakeCaseToCamelCase;
import org.klojang.util.IOMethods;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ColumnReaderFinderTest {
  private static final String DB_DIR = System.getProperty("user.home") + "/ColumnReaderFinderTest";
  private static final ThreadLocal<Connection> MY_CON = new ThreadLocal<>();

  public static final UUID uuid1 = UUID.randomUUID();
  public static final UUID uuid2 = UUID.randomUUID();
  public static final StringBuilder sb = new StringBuilder("foo");
  public static final URL url;

  static {
    try {
      url = new URL("http://example.com");
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  @BeforeEach
  public void before() throws IOException, SQLException {
    IOMethods.rm(DB_DIR);
    Files.createDirectories(Path.of(DB_DIR));
    Connection con = DriverManager.getConnection("jdbc:h2:" + DB_DIR + "/test");
    String sql = """
          CREATE TABLE STUFF(
            uuid1 VARCHAR(255),
            uuid2 UUID,
            url VARCHAR(255),
            sb VARCHAR(255))
          """;
    SQL.simple(sql).session(con).prepareUpdate().execute();
    Stuff stuff = new Stuff(uuid1, uuid2, url, sb);
    SQL.configureInsert()
          .of(Stuff.class)
          .into("STUFF")
          .retrieveKeys(false)
          .prepare(con)
          .bind(stuff)
          .execute();
    MY_CON.set(con);
  }

  @Test
  public void test00() {
    String sql = "SELECT * FROM STUFF";
    SQLSession session = SQL.template(sql).session(MY_CON.get());
    Stuff stuff = session.prepareQuery()
          .withNameMapper(new SnakeCaseToCamelCase())
          .getBeanifier(Stuff.class)
          .beanify()
          .get();
    assertEquals(uuid1, stuff.uuid1());
    assertEquals(uuid2, stuff.uuid2());
    assertEquals(url, stuff.url());
    assertEquals(sb.toString(), stuff.sb().toString());
  }

  @AfterEach
  public void after() throws SQLException, IOException {
    if (MY_CON.get() != null) {
      MY_CON.get().close();
    }
    IOMethods.rm(DB_DIR);
  }

}
