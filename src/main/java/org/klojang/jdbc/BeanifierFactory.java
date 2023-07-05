package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.rs.BeanChannel;
import org.klojang.templates.NameMapper;
import org.klojang.util.ExceptionMethods;
import org.klojang.util.InvokeMethods;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static org.klojang.jdbc.x.rs.BeanChannel.createChannels;

/**
 * <p>A factory for {@link ResultSetBeanifier} instances. All
 * {@link ResultSet} objects passed to a single {@code BeanifierFactory} instance must be
 * created from the same SQL query. The very first {@code ResultSet} passed to its
 * {@link #getBeanifier(ResultSet) getBeanifier()} method is used to configure the
 * conversion from the {@code ResultSet} into a JavaBean. Subsequent calls to
 * {@code getBeanifier()} will use the same configuration. Passing heterogeneous result
 * set to one and the same {@code BeanifierFactory} instance will produce undefined
 * results.
 *
 * <p>(More precisely: all result sets must have the same number of columns and the same
 * column types in the same order. Column names/labels do in fact not matter. The
 * column-to-property mapping is set up and fixed after the first call to
 * {@code getBeanifier()}. Thus, strictly speaking, the SQL query itself is not the
 * defining factor.)
 *
 * @param <T> the type of JavaBeans produced by the beanifier obtained from the
 *     {@code BeanifierFactory}
 * @author Ayco Holleman
 */
@SuppressWarnings("rawtypes")
public class BeanifierFactory<T> {

  private final AtomicReference<BeanChannel[]> ref = new AtomicReference<>();
  private final ReentrantLock lock = new ReentrantLock();

  private final Class<T> beanClass;
  private final Supplier<T> beanSupplier;
  private final NameMapper mapper;

  /**
   * Creates a new {@code BeanifierFactory}. Column names will be mapped as-is to property
   * names.
   *
   * @param beanClass the class of the JavaBeans that will be produced by beanifiers
   *     obtained from this {@code BeanifierFactory}
   */
  public BeanifierFactory(Class<T> beanClass) {
    this(beanClass, () -> newInstance(beanClass), NameMapper.AS_IS);
  }

  /**
   * Creates a new {@code BeanifierFactory}. Column names will be mapped as-is to property
   * names.
   *
   * @param beanClass the class of the JavaBeans that the {@code BeanifierFactory}
   *     will be catering for
   * @param beanSupplier the supplier of the JavaBeans. This would ordinarily be a
   *     method reference to the constructor of the JavaBean (e.g. {@code Employee::new})
   */
  public BeanifierFactory(Class<T> beanClass, Supplier<T> beanSupplier) {
    this(beanClass, beanSupplier, NameMapper.AS_IS);
  }

  /**
   * Creates a new {@code BeanifierFactory}.
   *
   * @param beanClass the class of the JavaBeans that will be produced by beanifiers
   *     obtained from this {@code BeanifierFactory}
   * @param columnToPropertyMapper a {@code NameMapper} mapping column names to
   *     property names
   */
  public BeanifierFactory(Class<T> beanClass, NameMapper columnToPropertyMapper) {
    this(beanClass, () -> newInstance(beanClass), columnToPropertyMapper);
  }

  /**
   * Creates a new {@code BeanifierFactory}.
   *
   * @param beanClass the class of the JavaBeans that will be produced by beanifiers
   *     obtained from this {@code BeanifierFactory}
   * @param beanSupplier the supplier of the JavaBeans
   * @param columnToPropertyMapper a {@code NameMapper} mapping column names to
   *     property names
   */
  public BeanifierFactory(
      Class<T> beanClass, Supplier<T> beanSupplier, NameMapper columnToPropertyMapper) {
    this.beanClass = Check.notNull(beanClass, "beanClass").ok();
    this.beanSupplier = Check.notNull(beanSupplier, "beanSupplier").ok();
    this.mapper = Check.notNull(columnToPropertyMapper, "columnToPropertyMapper").ok();
  }

  /**
   * Returns a {@code ResultSetBeanifier} that will convert the rows in the specified
   * {@code ResultSet} into JavaBeans of type {@code <T>}.
   *
   * @param rs the {@code ResultSet}
   * @return A {@code ResultSetBeanifier} that will convert the rows in the specified
   *     {@code ResultSet} into JavaBeans of type {@code <T>}
   * @throws SQLException
   */
  public ResultSetBeanifier<T> getBeanifier(ResultSet rs) throws SQLException {
    if (!rs.next()) {
      return EmptyBeanifier.INSTANCE;
    }
    BeanChannel[] channels;
    if ((channels = ref.getPlain()) == null) {
      lock.lock();
      try {
        if (ref.get() == null) {
          ref.set(channels = createChannels(rs, beanClass, mapper));
        }
      } finally {
        lock.unlock();
      }
    }
    return new DefaultBeanifier<>(rs, channels, beanSupplier);
  }

  private static <U> U newInstance(Class<U> beanClass) {
    try {
      return InvokeMethods.newInstance(beanClass);
    } catch (Exception e) {
      throw ExceptionMethods.uncheck(e);
    }
  }
}
