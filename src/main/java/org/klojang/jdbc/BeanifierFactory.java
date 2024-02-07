package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.rs.PropertyWriter;
import org.klojang.jdbc.x.rs.RecordFactory;
import org.klojang.templates.NameMapper;
import org.klojang.util.ExceptionMethods;
import org.klojang.util.InvokeMethods;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static org.klojang.jdbc.x.rs.PropertyWriter.createWriters;
import static org.klojang.templates.NameMapper.AS_IS;

/**
 * <p>A factory for {@link ResultSetBeanifier} instances. Generally you would create one
 * {@code BeanifierFactory} per SQL query. If multiple types of beans are extracted from
 * the query result, you would create more than one {@code BeanifierFactory} per SQL
 * query. The very first {@link ResultSet} passed to
 * {@link #getBeanifier(ResultSet) BeanifierFactory.getBeanifier()} is used to configure
 * the extraction process. Subsequent calls to {@code getBeanifier()} will use the same
 * configuration. Therefore, although multiple {@code BeanifierFactory} instances may be
 * instantiated for a single SQL query, a single {@code BeanifierFactory} should not be
 * used to process result sets from different SQL queries.
 *
 * <p><i>(More precisely: all result sets subsequently passed to
 * {@link #getBeanifier(ResultSet) getBeanifier()} must have the same number of columns,
 * and they must have the same column types in the same order. Column names do not matter.
 * Thus, you <b>could</b>, in fact, use a single {@code BeanifierFactory} for multiple SQL
 * queries &#8212; for example if they all select a primary key column and (say) a
 * {@code DESCRIPTION} column from different tables. This might be the case for web
 * applications that need to fill multiple {@code <select>}) boxes.)</i>
 *
 * @param <T> the type of JavaBeans or records produced by the beanifier
 * @author Ayco Holleman
 */
@SuppressWarnings("rawtypes")
public final class BeanifierFactory<T> {

  private static final String BEAN_CLASS = "bean class";
  private static final String BEAN_SUPPLIER = "bean supplier";
  private static final String COLUMN_TO_PROPERTY_MAPPER = "column-to-property mapper";
  private static final String BEAN_SUPPLIER_NOT_SUPPORTED = "bean supplier not supported for records";

  /**
   * The object held by the AtomicReference will either be a RecordFactory in case the
   * bean class is a record type or a PropertyWriter[] in any other case.
   */
  private final AtomicReference<Object> ref = new AtomicReference<>();
  private final ReentrantLock lock = new ReentrantLock();

  private final Class<T> beanClass;
  private final Supplier<T> beanSupplier;
  private final NameMapper mapper;

  /**
   * Creates a new {@code BeanifierFactory}. Column names will be mapped as-is to property
   * names.
   *
   * @param beanClass the class of the JavaBeans that will be produced by beanifiers
   *       obtained from this {@code BeanifierFactory}
   */
  public BeanifierFactory(Class<T> beanClass) {
    Check.notNull(beanClass, BEAN_CLASS);
    this.beanClass = beanClass;
    this.beanSupplier = beanClass.isRecord() ? null : () -> newInstance(beanClass);
    this.mapper = AS_IS;
  }

  /**
   * Creates a new {@code BeanifierFactory}. Column names will be mapped as-is to property
   * names.
   *
   * @param beanClass the class of the JavaBeans that the {@code BeanifierFactory}
   *       will be catering for
   * @param beanSupplier the supplier of the JavaBeans. This would ordinarily be a
   *       method reference to the constructor of the JavaBean (e.g.
   *       {@code Employee::new}). An {@link IllegalArgumentException} is thrown if
   *       {@code beanClass} is a {@code record} type.
   */
  public BeanifierFactory(Class<T> beanClass, Supplier<T> beanSupplier) {
    Check.notNull(beanClass, BEAN_CLASS);
    Check.that(beanClass).isNot(Class::isRecord, BEAN_SUPPLIER_NOT_SUPPORTED);
    Check.notNull(beanSupplier, BEAN_SUPPLIER);
    this.beanClass = beanClass;
    this.beanSupplier = beanSupplier;
    this.mapper = AS_IS;
  }

  /**
   * Creates a new {@code BeanifierFactory}.
   *
   * @param beanClass the class of the JavaBeans that will be produced by beanifiers
   *       obtained from this {@code BeanifierFactory}
   * @param columnToPropertyMapper a {@code NameMapper} mapping column names to
   *       property names
   * @see org.klojang.templates.name.SnakeCaseToCamelCase
   */
  public BeanifierFactory(Class<T> beanClass, NameMapper columnToPropertyMapper) {
    Check.notNull(beanClass, BEAN_CLASS);
    Check.notNull(columnToPropertyMapper, COLUMN_TO_PROPERTY_MAPPER);
    this.beanClass = beanClass;
    this.beanSupplier = beanClass.isRecord() ? null : () -> newInstance(beanClass);
    this.mapper = columnToPropertyMapper;
  }

  /**
   * Creates a new {@code BeanifierFactory}.
   *
   * @param beanClass the class of the JavaBeans that will be produced by beanifiers
   *       obtained from this {@code BeanifierFactory}
   * @param beanSupplier the supplier of the JavaBeans. An
   *       {@link IllegalArgumentException} is thrown if {@code beanClass} is a
   *       {@code record} type.
   * @param columnToPropertyMapper a {@code NameMapper} mapping column names to
   *       property names
   */
  public BeanifierFactory(Class<T> beanClass,
        Supplier<T> beanSupplier,
        NameMapper columnToPropertyMapper) {
    Check.notNull(beanClass, BEAN_CLASS);
    Check.that(beanClass).isNot(Class::isRecord, BEAN_SUPPLIER_NOT_SUPPORTED);
    Check.notNull(beanSupplier, BEAN_SUPPLIER);
    Check.notNull(columnToPropertyMapper, COLUMN_TO_PROPERTY_MAPPER);
    this.beanClass = beanClass;
    this.beanSupplier = beanSupplier;
    this.mapper = columnToPropertyMapper;
  }

  /**
   * Returns a {@code ResultSetBeanifier} that will convert the rows in the specified
   * {@code ResultSet} into JavaBeans or records of type {@code <T>}.
   *
   * @param rs the {@code ResultSet}
   * @return A {@code ResultSetBeanifier} that will convert the rows in the specified
   *       {@code ResultSet} into JavaBeans or records of type {@code <T>}
   * @throws SQLException if a database error occurs
   */
  @SuppressWarnings("unchecked")
  public ResultSetBeanifier<T> getBeanifier(ResultSet rs) throws SQLException {
    return rs.next() ? beanClass.isRecord() ? getRecordBeanifier(rs) : getDefaultBeanifier(
          rs) : EmptyBeanifier.INSTANCE;
  }

  private DefaultBeanifier<T> getDefaultBeanifier(ResultSet rs) {
    PropertyWriter[] writers;
    if ((writers = (PropertyWriter[]) ref.getPlain()) == null) {
      lock.lock();
      try {
        if (ref.get() == null) {
          ref.set(writers = createWriters(rs, beanClass, mapper));
        }
      } finally {
        lock.unlock();
      }
    }
    return new DefaultBeanifier<>(rs, writers, beanSupplier);
  }

  @SuppressWarnings("unchecked")
  private RecordBeanifier getRecordBeanifier(ResultSet rs) {
    RecordFactory recordFactory;
    if ((recordFactory = (RecordFactory) ref.getPlain()) == null) {
      lock.lock();
      try {
        if (ref.get() == null) {
          ref.set(recordFactory = new RecordFactory<>((Class) beanClass, rs, mapper));
        }
      } finally {
        lock.unlock();
      }
    }
    return new RecordBeanifier<>(rs, recordFactory);
  }

  private static <U> U newInstance(Class<U> beanClass) {
    try {
      return InvokeMethods.newInstance(beanClass);
    } catch (Exception e) {
      throw ExceptionMethods.uncheck(e);
    }
  }
}
