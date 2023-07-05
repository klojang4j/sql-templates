package org.klojang.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import org.klojang.check.Check;
import org.klojang.templates.NameMapper;
import org.klojang.jdbc.x.rs.MapChannel;

import static org.klojang.jdbc.x.rs.MapChannel.createChannels;

/**
 * <p>A factory for {@link ResultSetMappifier} instances.
 * {@link ResultSet} objects passed to a single {@code MappifierFactory} instance must all
 * be created from the same SQL query. The very first {@code ResultSet} passed to its
 * {@link #getMappifier(ResultSet) getMappifier()} method is used to configure the
 * conversion from the {@code ResultSet} into a JavaBean. Subsequent calls to
 * {@code getMappifier()} will use the same configuration. Passing heterogeneous result
 * set to one and the same {@code MappifierFactory} instance will produce undefined
 * results.
 *
 * <p>(More precisely: all result sets must have the same number of columns and the same
 * column types in the same order. Column names/labels do in fact not matter. The
 * column-to-key mapping is set up and fixed after the first call to
 * {@code getMappifier()}. Thus, strictly speaking, the SQL query itself is not the
 * defining factor.)
 *
 * @author Ayco Holleman
 */
public final class MappifierFactory {

  private final AtomicReference<MapChannel<?>[]> ref = new AtomicReference<>();
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
   * Creates a new {@code MappifierFactory}.
   *
   * @param columnToKeyMapper a {@code NameMapper} mapping column names to map keys
   */
  public MappifierFactory(NameMapper columnToKeyMapper) {
    this.mapper = Check.notNull(columnToKeyMapper).ok();
  }

  public ResultSetMappifier getMappifier(ResultSet rs) throws SQLException {
    if (!rs.next()) {
      return EmptyMappifier.INSTANCE;
    }
    MapChannel<?>[] channels;
    if ((channels = ref.getPlain()) == null) {
      lock.lock();
      try {
        if (ref.get() == null) {
          ref.set(channels = createChannels(rs, mapper));
        }
      } finally {
        lock.unlock();
      }
    }
    return new DefaultMappifier(rs, channels);
  }
}
