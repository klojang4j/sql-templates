package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.Utils;
import org.klojang.jdbc.x.rs.KeyWriter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import static org.klojang.jdbc.x.rs.KeyWriter.createWriters;

/**
 * <p>A factory for {@link MapExtractor} instances. This class behaves analogously
 * to the {@link BeanExtractorFactory} class. See there for more details.
 *
 * @author Ayco Holleman
 */
public final class MapExtractorFactory {

  private final AtomicReference<KeyWriter<?>[]> ref = new AtomicReference<>();
  private final ReentrantLock lock = new ReentrantLock();

  private final SessionConfig config;

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
  public MapExtractor getMappifier(ResultSet rs) throws SQLException {
    if (!rs.next()) {
      return NoopMapExtractor.INSTANCE;
    }
    KeyWriter<?>[] writers;
    if ((writers = ref.getPlain()) == null) {
      lock.lock();
      try {
        if (ref.get() == null) {
          ref.set(writers = createWriters(rs, config));
        }
      } finally {
        lock.unlock();
      }
    }
    return new DefaultMapExtractor(rs, writers);
  }
}
