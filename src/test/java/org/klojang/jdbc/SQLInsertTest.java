package org.klojang.jdbc;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.klojang.check.aux.Result;
import org.klojang.convert.Morph;
import org.klojang.util.IOMethods;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static org.junit.jupiter.api.Assertions.*;

//@Disabled
public class SQLInsertTest {

  private static final String DB_DIR = System.getProperty("user.home") + "/klojang-db-insert-test";
  private static final ThreadLocal<Connection> MY_CON = new ThreadLocal<>();

  public static class Person {
    int id;
    String name;

    Person(String name) {
      this.name = name;
    }

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  public SQLInsertTest() { }

  @BeforeEach
  public void before() throws IOException, SQLException {
    IOMethods.rm(DB_DIR);
    Files.createDirectories(Path.of(DB_DIR));
    Connection c = DriverManager.getConnection("jdbc:h2:" + DB_DIR + "/test");
    String sql = "CREATE LOCAL TEMPORARY TABLE TEST(ID INT AUTO_INCREMENT, NAME VARCHAR(255))";
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

  /**
   * Baseline test that only uses JDBC and none of our own abstractions
   */
  @Test
  public void executeAndGetID00() throws SQLException {
    Connection con = MY_CON.get();
    String sql = "INSERT INTO TEST(NAME) VALUES(?)";
    try (PreparedStatement ps = con.prepareStatement(sql, RETURN_GENERATED_KEYS)) {
      ps.setString(1, "John");
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) {
        assertEquals("ID", rs.getMetaData().getColumnLabel(1));
        assertTrue(rs.next());
        // Not really interested in the actual auto-generated value
      }
    }
  }

  @Test
  public void executeAndGetID01() {
    String s = "INSERT INTO TEST(NAME) VALUES(:name)";
    Map<String, Object> data = Collections.singletonMap("name", "John");
    SQLSession sql = SQL.simple(s).session(MY_CON.get());
    long id = Long.MIN_VALUE;
    try (SQLInsert insert = sql.prepareInsert()) {
      insert.bind(data);
      id = insert.execute();
      assertTrue(id != Long.MIN_VALUE);
    }
  }

  @Test
  public void executeAndGetID02() {
    String s = "INSERT INTO TEST(NAME) VALUES(:name)";
    Person person = new Person("John");
    SQLSession sql = SQL.simple(s).session(MY_CON.get());
    long id = Long.MIN_VALUE;
    try (SQLInsert insert = sql.prepareInsert()) {
      insert.bind(person);
      id = insert.execute();
      assertTrue(id != Long.MIN_VALUE);
    }
  }

  @Test
  public void executeAndSetID00() {
    String s = "INSERT INTO TEST(NAME) VALUES(:name)";
    Map<String, Object> data = new HashMap<>(Collections.singletonMap("name", "John"));
    SQLSession sql = SQL.simple(s).session(MY_CON.get());
    try (SQLInsert insert = sql.prepareInsert()) {
      insert.bind(data, "id");
      insert.executeAndSetID();
      assertTrue(data.containsKey("id"));
    }
  }

  @Test
  public void executeAndSetID01() {
    String s = "INSERT INTO TEST(NAME) VALUES(:name)";
    Person person = new Person("John");
    person.setId(Integer.MIN_VALUE);
    SQLSession sql = SQL.simple(s).session(MY_CON.get());
    try (SQLInsert insert = sql.prepareInsert()) {
      insert.bind(person, "id");
      insert.executeAndSetID();
      assertTrue(person.getId() != Integer.MIN_VALUE);
    }
  }

  @Test
  public void executeAndSetID02() {
    Person person = new Person("John");
    person.setId(Integer.MIN_VALUE);
    try (SQLInsert insert = SQL
          .configureInsert()
          .of(Person.class)
          .into("TEST")
          .excluding("id")
          .prepare(MY_CON.get())) {
      insert.bind(person, "id");
      insert.executeAndSetID();
      assertTrue(person.getId() != Integer.MIN_VALUE);
    }
  }

  @Test
  public void insertBatch00() {
    try (SQLInsert insert = SQL
          .configureInsert()
          .of(Person.class)
          .into("TEST")
          .excluding("id")
          .prepare(MY_CON.get())) {
      insert.insertBatch(List.of(new Person("John"),
            new Person("Mark"),
            new Person("Edward")));
    }
    try (SQLQuery query = SQL.simple("SELECT COUNT(*) FROM TEST")
          .session(MY_CON.get())
          .prepareQuery()) {
      assertEquals(Result.of(3), query.getInt());
    }
  }

  @Test
  public void insertAllAndGetIDs00() {
    long[] ids;
    try (SQLInsert insert = SQL
          .configureInsert()
          .of(Person.class)
          .into("TEST")
          .excluding("id")
          .prepare(MY_CON.get())) {
      ids = insert.insertBatchAndGetIDs(
            List.of(new Person("John"),
                  new Person("Mark"),
                  new Person("Edward")));
    }
    assertEquals(3, ids.length);
    try (SQLQuery query = SQL.simple("SELECT ID FROM TEST")
          .session(MY_CON.get())
          .prepareQuery()) {
      long[] actual = Morph.convert(query.firstColumn(), long[].class);
      assertArrayEquals(ids, actual);
    }
  }

  @Test
  public void insertAllAndSetIDs00() {
    List<Person> beans = List.of(new Person("John"),
          new Person("Mark"),
          new Person("Edward"));
    try (SQLInsert insert = SQL
          .configureInsert()
          .of(Person.class)
          .into("TEST")
          .excluding("id")
          .prepare(MY_CON.get())) {
      insert.insertBatchAndSetIDs("id", beans);
    }
    int[] ids = beans.stream().mapToInt(Person::getId).toArray();
    try (SQLQuery query = SQL.simple("SELECT ID FROM TEST")
          .session(MY_CON.get())
          .prepareQuery()) {
      int[] actual = Morph.convert(query.firstColumn(), int[].class);
      assertArrayEquals(ids, actual);
    }
  }

}