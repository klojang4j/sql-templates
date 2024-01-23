package org.klojang.jdbc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.klojang.convert.Morph;
import org.klojang.util.IOMethods;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

//@Disabled
public class SQLBatchInsertTest {

  private static final String DB_DIR = System.getProperty("user.home") + "/klojang-db-batch-insert-test";
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

  public SQLBatchInsertTest() {}

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

  @Test
  public void insertBatch00() {
    try (SQLBatchInsert<Person> insert = SQL
          .configureBatchInsert()
          .of(Person.class)
          .into("TEST")
          .excluding("id")
          .prepare(MY_CON.get())) {
      insert.insertBatch(List.of(new Person("John"),
            new Person("Mark"),
            new Person("Edward")));
    }
    try (SQLQuery query = SQL.basic("SELECT COUNT(*) FROM TEST")
          .session(MY_CON.get())
          .prepareQuery()) {
      assertEquals(OptionalInt.of(3), query.getInt());
    }
  }

  @Test
  public void insertAllAndGetIDs00() {
    long[] ids;
    try (SQLBatchInsert<Person> insert = SQL
          .configureBatchInsert()
          .of(Person.class)
          .into("TEST")
          .excluding("id")
          .prepare(MY_CON.get())) {
      ids = insert.insertBatchAndGetIDs(List.of(new Person("John"),
            new Person("Mark"),
            new Person("Edward")));
    }
    assertEquals(3, ids.length);
    try (SQLQuery query = SQL.basic("SELECT ID FROM TEST")
          .session(MY_CON.get())
          .prepareQuery()) {
      long[] actual = Morph.convert(query.firstColumn(), long[].class);
      assertArrayEquals(ids, actual);
    }
  }

  @Test
  public void insertAllAndSetIDs00() {
    List<Person> persons = List.of(new Person("John"),
          new Person("Mark"),
          new Person("Edward"));
    try (SQLBatchInsert<Person> insert = SQL
          .configureBatchInsert()
          .of(Person.class)
          .into("TEST")
          .excluding("id")
          .prepare(MY_CON.get())) {
      insert.insertBatchAndSetIDs("id", persons);
    }
    int[] ids = persons.stream().mapToInt(Person::getId).toArray();
    try (SQLQuery query = SQL.basic("SELECT ID FROM TEST")
          .session(MY_CON.get())
          .prepareQuery()) {
      int[] actual = Morph.convert(query.firstColumn(), int[].class);
      assertArrayEquals(ids, actual);
    }
  }

}