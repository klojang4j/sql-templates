package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.Utils;
import org.klojang.jdbc.x.rs.PropertyWriter;
import org.klojang.jdbc.x.rs.RecordFactory;
import org.klojang.util.InvokeMethods;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static org.klojang.jdbc.x.Strings.*;
import static org.klojang.jdbc.x.rs.PropertyWriter.createWriters;
import static org.klojang.util.ClassMethods.className;

/**
 * <p>A factory for {@link BeanExtractor} instances. Generally you would create one
 * {@code BeanExtractorFactory} per SQL query. If multiple types of beans are extracted
 * from the query result (with different sets of columns feeding into different types of
 * beans), you would create more than one {@code BeanExtractorFactory} per SQL query. The
 * very first {@link ResultSet} passed to
 * {@link #getExtractor(ResultSet) BeanExtractorFactory.getExtractor()} is used to
 * configure the extraction process. {@code ResultSet} objects passed subsequently to
 * {@code getExtractor()} will be processed identically. Therefore, passing a
 * {@code ResultSet} that came from a completely different query will almost certainly
 * lead to unexpected outcomes or exceptions. In short: Multiple
 * {@code BeanExtractorFactory} instances may be created for a single SQL query, but a
 * single {@code BeanExtractorFactory} should not be used to handle multiple SQL queries.
 *
 * <p><i>(More precisely: all {@code ResultSet} objects subsequently passed to
 * {@link #getExtractor(ResultSet) getExtractor()} must have the same number of columns,
 * and they must have the same column types in the same order. Column names do not matter.
 * Thus, you <b>could</b>, in fact, use a single {@code BeanExtractorFactory} for multiple
 * SQL queries &#8212; for example if they all select the primary key column and, say, a
 * {@code VARCHAR} column from different tables. This might be the case for web
 * applications that need to fill multiple {@code <select>}) boxes.)</i>
 *
 * <p>It is not necessary to cache {@code BeanExtractorFactory} objects or
 * {@code BeanExtractor} objects. <i>Klojang JDBC</i> already caches the relevant data,
 * making both essentially light-weight objects. Create them when you need them.
 *
 * @param <T> the type of JavaBeans or records produced by the extractor
 * @author Ayco Holleman
 */
@SuppressWarnings("rawtypes")
public final class BeanExtractorFactory<T> {

  private record ExtractorID(Class clazz, Supplier supplier, SessionConfig config) {
    public int hashCode() {
      return Objects.hash(clazz, supplier, config);
    }

    public boolean equals(Object obj) {
      return this == obj || (obj instanceof ExtractorID e && clazz == e.clazz && supplier == e.supplier && config == e.config);
    }
  }

  private static final Map<ExtractorID, BeanExtractor> cache = new HashMap<>();

  private static final String RECORDS_NOT_ALLOWED = "bean supplier not supported for record type ${0}";

  private final Class<T> clazz;
  private final Supplier<T> supplier;
  private final SessionConfig config;

  /**
   * Creates a new {@code BeanExtractorFactory}.
   *
   * @param clazz the class of the JavaBeans or records that will be extracted by
   *       the {@code BeanExtractor} instances obtained from this
   *       {@code BeanExtractorFactory}
   */
  public BeanExtractorFactory(Class<T> clazz) {
    Check.notNull(clazz);
    this.clazz = clazz;
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
    if (!rs.next()) {
      return NoopBeanExtractor.INSTANCE;
    }
    ExtractorID key = new ExtractorID(clazz, supplier, config);
    BeanExtractor extractor = cache.get(key);
    if (extractor == null) {
      extractor = clazz.isRecord() ? recordExtractor(rs) : defaultExtractor(rs);
      cache.put(key, extractor);
    }
    return extractor;
  }

  private DefaultBeanExtractor<T> defaultExtractor(ResultSet rs) {
    PropertyWriter[] writers = createWriters(rs, clazz, config);
    return new DefaultBeanExtractor<>(rs, writers, supplier);
  }

  @SuppressWarnings("unchecked")
  private RecordExtractor recordExtractor(ResultSet rs) {
    RecordFactory factory = new RecordFactory<>((Class) clazz, rs, config);
    return new RecordExtractor<>(rs, factory);
  }

  private static <U> U newInstance(Class<U> clazz) {
    try {
      return InvokeMethods.newInstance(clazz);
    } catch (Exception e) {
      throw Utils.wrap(e);
    }
  }
}
