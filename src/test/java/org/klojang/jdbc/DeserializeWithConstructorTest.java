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
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeserializeWithConstructorTest {
  private static final String DB_DIR = System.getProperty("user.home") + "/klojang-jdbc-tests/DeserializeWithConstructorTest/h2";
  private static final ThreadLocal<Connection> MY_CON = new ThreadLocal<>();

  public record Dept(int id, String name) {
    private static int getId(String serialized) {
      return Integer.parseInt(serialized.split(";")[0]);
    }

    private static String getName(String serialized) {
      return serialized.split(";")[1];
    }

    public Dept(String serialized) {
      this(getId(serialized),getName(serialized));
    }

    public String toString() {
      return id + ";" + name;
    }
  }

  public record Emp(int id, String name, Dept dept) { }

  @BeforeEach
  public void before() throws IOException, SQLException {
    IOMethods.rm(DB_DIR);
    Files.createDirectories(Path.of(DB_DIR));
    Connection c = DriverManager.getConnection("jdbc:h2:" + DB_DIR + "/test");
    String sql = """
          CREATE LOCAL TEMPORARY TABLE EMP(
            ID INT, 
            NAME VARCHAR(255),
            DEPT VARCHAR(255))
          """;
    SQL.staticSQL(sql).session(c).execute();
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
  public static void afterAll() throws SQLException, IOException {
    if (MY_CON.get() != null) {
      MY_CON.get().close();
    }
    IOMethods.rm(DB_DIR);
  }

  @Test
  public void serializeWithToString00() {
    Emp emp0 = new Emp(1, "foo", new Dept(42, "bar"));
    SQLInsert insert = SQL.insert()
          .of(Emp.class)
          .retrieveKeys(false)
          .prepare(MY_CON.get());
    insert.bind(emp0).execute();
    insert.close();
    Emp emp1 = SQL.simpleQuery(MY_CON.get(), "SELECT * FROM EMP")
          .getExtractor(Emp.class)
          .extract()
          .get();

    assertEquals(emp0, emp1);
  }

  @Test
  public void serializeWithSerializer00() {
    SessionConfig config = new SessionConfig() {
      @Override
      public Function<Object, String> getSerializer(Class<?> beanType,
            String propertyName,
            Class<?> propertyType) {
        if (propertyType.equals(Dept.class)) {
          return x -> {
            Dept d = (Dept) x;
            // make sure we're really using the serializer, and not toString()
            // to serialize the department, so specify an id that is not the id
            // in the Emp object we are about to save
            return 77 + ";" + d.name();
          };
        }
        return null;
      }
    };
    Emp emp0 = new Emp(1, "foo", new Dept(42, "bar"));
    SQLInsert insert = SQL.insert()
          .of(Emp.class)
          .retrieveKeys(false)
          .withConfig(config)
          .prepare(MY_CON.get());
    insert.bind(emp0).execute();
    insert.close();
    Emp emp1 = SQL.simpleQuery(MY_CON.get(), "SELECT * FROM EMP")
          .getExtractor(Emp.class)
          .extract()
          .get();
    assertEquals(77, emp1.dept().id());
    assertEquals("bar", emp1.dept().name());
  }

  @Test
  public void serializeWithCustomBinder00() {
    SessionConfig config = new SessionConfig() {
      @Override
      public CustomBinder getCustomBinder(Class<?> beanType,
            String propertyName,
            Class<?> propertyType) {
        if (propertyType.equals(Dept.class)) {
          return (ps, idx, val) -> {
            ps.setString(idx, "77;bar");
          };
        }
        return null;
      }
    };
    Emp emp0 = new Emp(1, "foo", new Dept(42, "bar"));
    SQLInsert insert = SQL.insert()
          .of(Emp.class)
          .retrieveKeys(false)
          .withConfig(config)
          .prepare(MY_CON.get());
    insert.bind(emp0).execute();
    insert.close();
    Emp emp1 = SQL.simpleQuery(MY_CON.get(), "SELECT * FROM EMP")
          .getExtractor(Emp.class)
          .extract()
          .get();
    assertEquals(77, emp1.dept().id());
    assertEquals("bar", emp1.dept().name());
  }

}
