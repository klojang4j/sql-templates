package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.Utils;
import org.klojang.jdbc.x.rs.KeyWriter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import static org.klojang.jdbc.x.rs.KeyWriter.createWriters;

/**
 * <p>A factory for {@link MapExtractor} instances. This class behaves analogously
 * to the {@link BeanExtractorFactory} class. See the comments for that class for more
 * details.
 *
 * <p>Note that a single {@code MapExtractor} can more easily be used for multiple
 * queries than a {@link BeanExtractor} (again, see the comments for the
 * {@link BeanExtractorFactory} class):
 *
 * <blockquote><pre>{@code
 * import static org.klojang.jdbc.SQL.simpleQuery;
 * import static org.klojang.jdbc.SQL.staticSQL;
 *
 * // ...
 *
 * Connection con = ...
 *
 * String sql = "CREATE TABLE EMPLOYEE(EMP_ID INT AUTO_INCREMENT, EMP_NAME VARCHAR(32))";
 * staticSQL(sql).session(con).execute();
 * sql = "CREATE TABLE DEPARTMENT(DEPT_ID INT AUTO_INCREMENT, DEPT_NAME VARCHAR(32))";
 * staticSQL(sql).session(con).execute();
 *
 * staticSQL("INSERT INTO EMPLOYEE(EMP_NAME) VALUES('Foo')").session(con).execute();
 * staticSQL("INSERT INTO DEPARTMENT(DEPT_NAME) VALUES('Bar')").session(con).execute();
 *
 * MapExtractorFactory factory = new MapExtractorFactory();
 *
 * ResultSet rs = simpleQuery(con,"SELECT EMP_ID AS ID, EMP_NAME AS NAME FROM EMPLOYEE").execute();
 * MapExtractor sharedExtractor = factory.getExtractor(rs);
 * List<Map<String, Object>> emps = sharedExtractor.extractAll();
 * assertEquals("Foo", emps.get(0).get("name"));
 *
 * rs = simpleQuery(con, "SELECT DEPT_ID AS ID, DEPT_NAME AS NAME FROM DEPARTMENT").execute();
 * sharedExtractor = factory.getExtractor(rs);
 * List<Map<String, Object>> depts = sharedExtractor.extractAll();
 * assertEquals("Bar", depts.get(0).get("name"));
 * }</pre></blockquote>
 *
 * @author Ayco Holleman
 */
public final class MapExtractorFactory {

  private final ReentrantLock lock = new ReentrantLock();

  private final SessionConfig config;

  private KeyWriter<?>[] payload;

  /**
   * Creates a new {@code MapExtractorFactory}.
   */
  public MapExtractorFactory() {
    this(Utils.DEFAULT_CONFIG);
  }

  /**
   * Creates a new {@code MapExtractorFactory} using the specified configuration object
   *
   * @param config a {@code SessionConfig} object that allows you to fine-tune the
   *       behaviour of the {@code MapExtractor}
   */
  public MapExtractorFactory(SessionConfig config) {
    this.config = Check.notNull(config).ok();
  }

  /**
   * Returns a {@code MapExtractor} that will convert the rows in the specified
   * {@code ResultSet} into {@code Map<String, Object>} pseudo-objects.
   *
   * @param rs the {@code ResultSet}
   * @return a {@code MapExtractor} that will convert the rows in the specified
   *       {@code ResultSet} into {@code Map<String, Object>} pseudo-objects.
   * @throws SQLException if a database error occurs
   */
  public MapExtractor getExtractor(ResultSet rs) throws SQLException {
    if (!rs.next()) {
      return NoopMapExtractor.INSTANCE;
    }
    KeyWriter<?>[] writers;
    if ((writers = payload) == null) {
      lock.lock();
      try {
        // Check again, but now we know for sure the only one in here
        if (payload == null) {
          payload = writers = createWriters(rs, config);
        }
      } finally {
        lock.unlock();
      }
    }
    return new DefaultMapExtractor(rs, writers);
  }
}
