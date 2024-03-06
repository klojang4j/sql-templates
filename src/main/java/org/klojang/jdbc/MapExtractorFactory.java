package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.Utils;
import org.klojang.jdbc.x.rs.KeyWriter;

import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static org.klojang.jdbc.x.rs.KeyWriter.createWriters;

/**
 * <p>A factory for {@link MapExtractor} instances. This class behaves similarly to the
 * {@link BeanExtractorFactory} class. See the comments for that class for more details.
 *
 * <p>A single {@code MapExtractor} can more easily be used for multiple queries than a
 * {@link BeanExtractor} (again, see the comments for the {@link BeanExtractorFactory}
 * class):
 *
 * <blockquote><pre>{@code
 * static final MapExtractorFactory FACTORY = new MapExtractorFactory();
 *
 *  // ...
 *
 *  MapExtractor sharedExtractor;
 *
 * String sql = "SELECT EMP_ID AS ID, EMP_NAME AS NAME FROM EMPLOYEE";
 * try(ResultSet rs = ...) { // execute SQL and get ResultSet
 *   sharedExtractor = factory.getExtractor(rs);
 *   Map<String, Object> emp = sharedExtractor.extract().get();
 *   assertEquals("John Smith", emp.get("name"));
 * }
 *
 * String sql = "SELECT DEPT_ID AS ID, DEPT_NAME AS NAME FROM DEPARTMENT;
 * try(ResultSet rs = ...) {
 *   // Can still use the same MapExtractor because column labels
 *   // and column types are the same
 *   Map<String, Object> dept = sharedExtractor.extract().get();
 *   assertEquals("Sales", dept.get("name"));
 * }
 * }</pre></blockquote>
 *
 * <p>However, you don't gain much in terms of performance, because, once a
 * {@code MapExtractorFactory} has configured itself using the very first
 * {@code ResultSet} passed to it, it does not impose any further overhead.
 *
 * @author Ayco Holleman
 */
public final class MapExtractorFactory implements ExtractorFactory<Map<String, Object>> {

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
   */
  public MapExtractor getExtractor(ResultSet rs) {
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
