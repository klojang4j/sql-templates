package org.klojang.jdbc;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

/**
 * <p>Converts the rows in a JDBC {@link ResultSet} into JavaBeans or records. Instances
 * are obtained via
 * {@link BeanExtractorFactory#getExtractor(ResultSet) BeanExtractorFactory.getBeanifier()}. A
 * {@code BeanExtractor} is agnostic about, and has no opinion on how the
 * {@link ResultSet} was obtained. It may have been produced using regular JDBC calls. It
 * does not aim to be an ORM-like class. It just converts result sets into (flat)
 * JavaBeans, to be carried across the boundary of the data access module (and into the
 * view layer, for example). The JavaBeans may have nested structures, but only top-level
 * properties will be populated.
 *
 * <p>{@code BeanExtractor} is an extension of the {@link Iterable} interface,
 * enabling you to extract the JavaBeans in a {@code forEach} loop:
 *
 * <blockquote><pre>{@code
 * ResultSet rs = ...;
 * BeanExtractorFactory factory = new BeanExtractorFactory(Employee.class);
 * for(Employee emp : factory.getBeanifier(rs)) {
 *   // do stuff ...
 * }
 * }</pre></blockquote>
 *
 * <h2>JavaBeans vs. Records</h2>
 * When converting a row into a JavaBean, a {@code BeanExtractor} will always use the
 * setters on the JavaBean to populate it. There is no way to populate the bean via its
 * constructors. When converting to a {@code record}, a {@code BeanExtractor} will
 * obviously always use one of its constructors to populate it. Make sure the record has a
 * constructor for those record components that are supposed to map to columns in the
 * SELECT clause. The encounter order of the record components within the constructor must
 * match the encounter order of the columns in the SELECT clause. However, it is not
 * necessary that each record component can be mapped to a column in the SELECT clause,
 * and it is not necessary that each column in the SELECT clause can be mapped back to a
 * record component.
 *
 * @param <T> the type of the JavaBeans or records produced by the
 *       {@code BeanExtractor}
 * @author Ayco Holleman
 * @see BeanExtractorFactory
 * @see MapExtractor
 */
public sealed interface BeanExtractor<T> extends Iterable<T>
      permits DefaultBeanExtractor, NoopBeanExtractor, RecordExtractor {

  /**
   * Converts the current row within the {@code ResultSet} into a JavaBean. If the
   * {@code ResultSet} is empty, or if there are no more rows in the {@code ResultSet}, an
   * empty {@code Optional} is returned. You can keep calling {@code extract()} to
   * successively extract all rows in the result set until you receive an empty
   * {@code Optional}, or until {@link #isEmpty()} returns {@code true}.
   *
   * @return an {@code Optional} containing the JavaBean or an empty {@code Optional} if
   *       the {@code ResultSet} contained no (more) rows
   */
  Optional<T> extract();

  /**
   * Converts at most {@code limit} rows from the {@code ResultSet} into JavaBeans,
   * possibly less, if there are not that many rows left in the {@code ResultSet}. If the
   * {@code ResultSet} is empty, or if there are no more rows in the {@code ResultSet}, an
   * empty {@code List} is returned.
   *
   * @param limit the maximum number of rows to extract and convert
   * @return a {@code List} of JavaBeans or an empty {@code List} if the {@code ResultSet}
   *       contained no (more) rows
   */
  List<T> extract(int limit);

  /**
   * Converts all remaining rows in the {@code ResultSet} into JavaBeans.
   *
   * @return a {@code List} of JavaBeans or an empty {@code List} if the {@code ResultSet}
   *       contained no (more) rows
   */
  List<T> extractAll();

  /**
   * Converts all remaining rows in the {@code ResultSet} into JavaBeans.
   *
   * @param sizeEstimate an estimate of the size of the resulting {@code List}.
   * @return a {@code List} of JavaBeans or an empty {@code List} if the {@code ResultSet}
   *       contained no (more) rows
   */
  List<T> extractAll(int sizeEstimate);

  /**
   * Returns {@code true} if the end of the {@code ResultSet} has been reached;
   * {@code false} otherwise.
   *
   * @return whether the end of the {@code ResultSet} has been reached
   */
  boolean isEmpty();

}
