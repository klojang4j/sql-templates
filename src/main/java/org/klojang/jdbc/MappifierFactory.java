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
 * A factory for {@link ResultSetMappifier} instances. The {@link ResultSet} objects
 * passed to a {@code MappifierFactory} must all be created from the same SQL query. More
 * precisely, they may have been created from different queries, but the <i>number</i> and
 * the <i>types</i> of the columns in their SELECT clause must be the same. The first
 * {@link ResultSet} passed to a {@code MappifierFactory} is used the determine the key
 * names. Subsequent {@link ResultSet} objects need not have the same column
 * <i>names</i>.
 *
 * @author Ayco Holleman
 */
public final class MappifierFactory {

  private final ReentrantLock lock = new ReentrantLock();
  private final AtomicReference<MapChannel<?>[]> ref = new AtomicReference<>();
  private final NameMapper mapper;

  /**
   * Creates a new {@code MappifierFactory}. Column names will be mapped
   * {@link NameMapper#AS_IS as-is} to map keys.
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

  public ResultSetMappifier getResultSetMappifier(ResultSet rs) throws SQLException {
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
