package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.Utils;
import org.klojang.jdbc.x.rs.PropertyWriter;
import org.klojang.jdbc.x.rs.RecordExtractor;
import org.klojang.jdbc.x.rs.RecordFactory;
import org.klojang.util.InvokeMethods;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static org.klojang.jdbc.x.Strings.*;
import static org.klojang.jdbc.x.rs.PropertyWriter.createWriters;
import static org.klojang.util.ClassMethods.className;

/**
 * <p>A factory for {@link BeanExtractor} instances. Generally you would create one
 * {@code BeanExtractorFactory} per SQL query. If multiple types of beans are extracted
 * from the query result (different columns feeding into different types of beans), you
 * would create more than one {@code BeanExtractorFactory} per SQL query. The very first
 * {@link ResultSet} passed to
 * {@link #getExtractor(ResultSet) BeanExtractorFactory.getExtractor()} is used to
 * configure the extraction process. {@code ResultSet} objects subsequently passed to
 * {@code getExtractor()} will be processed identically. It will be assumed they have the
 * same number of columns and the same column types in the same order. Therefore, passing
 * a {@code ResultSet} that came from a completely different query will almost certainly
 * lead to unexpected outcomes or exceptions.
 *
 * <p>Because the configuration of the extraction process is somewhat expensive, it
 * is recommended that you store {@code BeanExtractorFactory} instances in
 * {@code private static final} fields or use some sort of caching mechanism.
 *
 * @param <T> the type of JavaBeans or records produced by the extractor
 * @author Ayco Holleman
 */
public final class BeanExtractorFactory<T> {

  private static final String RECORDS_NOT_ALLOWED
        = "bean supplier not supported for immutable type ${0}";

  private final ReentrantLock lock = new ReentrantLock();

  private final Class<T> clazz;
  private final Supplier<T> supplier;
  private final SessionConfig config;

  /**
   * Will either be a RecordFactory in case clazz is a record type or a PropertyWriter[]
   * array in case it is a JavaBean type.
   */
  private Object payload;

  /**
   * Creates a new {@code BeanExtractorFactory}.
   *
   * @param clazz the class of the JavaBeans or records that will be extracted by
   *       the {@code BeanExtractor} instances obtained from this
   *       {@code BeanExtractorFactory}
   */
  public BeanExtractorFactory(Class<T> clazz) {
    this.clazz = Check.notNull(clazz).ok();
    this.supplier = clazz.isRecord() ? null : () -> newInstance(clazz);
    this.config = Utils.DEFAULT_CONFIG;
  }

  /**
   * Creates a new {@code BeanExtractorFactory}.
   *
   * @param clazz the class of the JavaBeans or records that will be extracted by
   *       the {@code BeanExtractor} instances obtained from this
   *       {@code BeanExtractorFactory}
   * @param config a {@code SessionConfig} object that allows you to fine-tune the
   *       behaviour of the {@code BeanExtractor}
   */
  public BeanExtractorFactory(Class<T> clazz, SessionConfig config) {
    this.clazz = Check.notNull(clazz, BEAN_CLASS).ok();
    this.supplier = clazz.isRecord() ? null : () -> newInstance(clazz);
    this.config = Check.notNull(config, CONFIG).ok();
  }

  /**
   * Creates a new {@code BeanExtractorFactory}. The provided class must <i>not</i> be a
   * {@code record} type.
   *
   * @param clazz the class of the JavaBeans that will be extracted by the
   *       {@code BeanExtractor} instances obtained from this
   *       {@code BeanExtractorFactory}
   * @param beanSupplier the supplier of the JavaBeans. This would ordinarily be a
   *       method reference to the constructor of the JavaBean (e.g.
   *       {@code Employee::new}).
   */
  public BeanExtractorFactory(Class<T> clazz, Supplier<T> beanSupplier) {
    this.clazz = Check.notNull(clazz, BEAN_CLASS)
          .isNot(Class::isRecord, RECORDS_NOT_ALLOWED, className(clazz))
          .ok();
    this.supplier = Check.notNull(beanSupplier, BEAN_SUPPLIER).ok();
    this.config = Utils.DEFAULT_CONFIG;
  }

  /**
   * Creates a new {@code BeanExtractorFactory}. The provided class must <i>not</i> be a
   * {@code record} type.
   *
   * @param clazz the class of the JavaBeans that will be extracted by the
   *       {@code BeanExtractor} instances obtained from this
   *       {@code BeanExtractorFactory}
   * @param beanSupplier the supplier of the JavaBeans.
   * @param config a {@code SessionConfig} object that allows you to fine-tune the
   *       behaviour of the {@code BeanExtractor}
   */
  public BeanExtractorFactory(Class<T> clazz,
        Supplier<T> beanSupplier,
        SessionConfig config) {
    this.clazz = Check.notNull(clazz, BEAN_CLASS)
          .isNot(Class::isRecord, RECORDS_NOT_ALLOWED, className(clazz))
          .ok();
    this.supplier = Check.notNull(beanSupplier, BEAN_SUPPLIER).ok();
    this.config = Check.notNull(config, CONFIG).ok();
  }

  /**
   * Returns a {@code BeanExtractor} that will convert the rows in the specified
   * {@code ResultSet} into JavaBeans or records of type {@code <T>}.
   *
   * @param rs the {@code ResultSet}
   * @return A {@code BeanExtractor} that will convert the rows in the specified
   *       {@code ResultSet} into JavaBeans or records of type {@code <T>}
   * @throws SQLException if a database error occurs
   */
  @SuppressWarnings("unchecked")
  public BeanExtractor<T> getExtractor(ResultSet rs) throws SQLException {
    return rs.next() ? clazz.isRecord()
          ? recordExtractor(rs) : defaultExtractor(rs)
          : NoopBeanExtractor.INSTANCE;
  }

  private DefaultBeanExtractor<T> defaultExtractor(ResultSet rs) {
    PropertyWriter<?, ?>[] writers;
    if ((writers = (PropertyWriter<?, ?>[]) payload) == null) {
      lock.lock();
      try {
        // Check again, but now we know for sure the only one in here
        if (payload == null) {
          payload = writers = createWriters(rs, clazz, config);
        }
      } finally {
        lock.unlock();
      }
    }
    return new DefaultBeanExtractor<>(rs, writers, supplier);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private RecordExtractor recordExtractor(ResultSet rs) {
    RecordFactory recordFactory;
    if ((recordFactory = (RecordFactory) payload) == null) {
      lock.lock();
      try {
        if (payload == null) {
          payload = recordFactory = new RecordFactory<>((Class) clazz, rs, config);
        }
      } finally {
        lock.unlock();
      }
    }
    return new RecordExtractor<>(rs, recordFactory);
  }


  private static <U> U newInstance(Class<U> clazz) {
    try {
      return InvokeMethods.newInstance(clazz);
    } catch (Exception e) {
      throw Utils.wrap(e);
    }
  }
}
