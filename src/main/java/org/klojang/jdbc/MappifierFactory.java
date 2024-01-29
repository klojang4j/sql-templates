package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.rs.KeyWriter;
import org.klojang.templates.NameMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import static org.klojang.jdbc.x.rs.KeyWriter.createWriters;

/**
 * <p>A factory for {@link ResultSetMappifier} instances. This class behaves analogously
 * to the {@link BeanifierFactory} class. See there for more details.
 *
 * @author Ayco Holleman
 */
public final class MappifierFactory {

  private final AtomicReference<KeyWriter<?>[]> ref = new AtomicReference<>();
  private final ReentrantLock lock = new ReentrantLock();

  private final NameMapper mapper;

  /**
   * Creates a new {@code MappifierFactory}. Column names will be mapped as-is to map
   * keys.
   */
  public MappifierFactory() {
    this(NameMapper.AS_IS);
  }

  /**
   * Creates a new {@code MappifierFactory} using the specified column-to-key mapper.
   *
   * @param columnToKeyMapper a {@code NameMapper} mapping column names to map keys
   * @see org.klojang.templates.name.SnakeCaseToCamelCase
   */
  public MappifierFactory(NameMapper columnToKeyMapper) {
    this.mapper = Check.notNull(columnToKeyMapper).ok();
  }

  /**
   * Returns a {@code ResultSetMappifier} that will convert the rows in the specified
   * {@code ResultSet} into {@code Map<String, Object>} pseudo-objects.
   *
   * @param rs the {@code ResultSet}
   * @return a {@code ResultSetMappifier} that will convert the rows in the specified
   *       {@code ResultSet} into {@code Map<String, Object>} pseudo-objects.
   * @throws SQLException if a database error occurs
   */
  public ResultSetMappifier getMappifier(ResultSet rs) throws SQLException {
    if (!rs.next()) {
      return EmptyMappifier.INSTANCE;
    }
    KeyWriter<?>[] writers;
    if ((writers = ref.getPlain()) == null) {
      lock.lock();
      try {
        if (ref.get() == null) {
          ref.set(writers = createWriters(rs, mapper));
        }
      } finally {
        lock.unlock();
      }
    }
    return new DefaultMappifier(rs, writers);
  }
}
