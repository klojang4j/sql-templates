package org.klojang.jdbc;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

/**
 * <p>Converts the rows in a JDBC {@link ResultSet} into JavaBeans, records or
 * {@code Map<String, Object>} pseudo-objects. For brevity's sake we will call all three
 * types of objects "beans" from now on. Instances of this interface are obtained via
 * {@link BeanExtractorFactory#getExtractor(ResultSet)
 * BeanExtractorFactory.getExtractor()} or via
 * {@link MapExtractorFactory#getExtractor(ResultSet) MapExtractorFactory.getExtractor()}.
 * A {@code BeanExtractor} is agnostic about, and has no opinion on how the
 * {@link ResultSet} was obtained. It may have been produced using regular JDBC calls. It
 * does not aim to be an ORM-like class. It just converts result sets into (flat) beans,
 * to be carried across the boundary of the data access module (and into the view layer,
 * for example). The beans may have nested structures, but only top-level properties will
 * be populated.
 *
 * <p>{@code BeanExtractor} is an extension of the {@link Iterable} interface,
 * enabling you to extract the beans in a {@code forEach} loop:
 *
 * <blockquote><pre>{@code
 * static final BeanExtractorFactory FACTORY = new BeanExtractorFactory(Employee.class);
 *
 * // ...
 *
 * try(ResultSet rs = ...) {
 *   for(Employee emp : FACTORY.getExtractor(rs)) {
 *     // do stuff ...
 *   }
 * }
 * }</pre></blockquote>
 *
 * <h2>JavaBeans vs. Records</h2>
 * When converting a row into a JavaBean, a {@code BeanExtractor} will always use the
 * setters on the JavaBean to populate it. There is no way to populate a JavaBean via its
 * constructors. When converting to a {@code record}, a {@code BeanExtractor} will
 * obviously always use one of its constructors to populate it. Make sure the record has a
 * constructor for those record components that are supposed to map to columns in the
 * SELECT clause. The encounter order of the record components within the constructor must
 * match the encounter order of the columns in the SELECT clause. However, it is not
 * necessary that each record component can be mapped to a column in the SELECT clause,
 * and it is not necessary that each column in the SELECT clause can be mapped back to a
 * record component.
 *
 * <h2>Map Extractors</h2>
 *
 * Although the {@code BeanExtractor} interface also allows for the extraction of
 * {@code Map<String, Object>} pseudo-objects from a {@code ResultSet}, there still is a
 * specialization of the {@code BeanExtractor} interface specifically for this purpose:
 * the {@link MapExtractor} interface. This interface does not contain any methods, but it
 * reduces the verbosity of your code and, more importantly, it reflects the fact that map
 * extractors are configured and obtained by other means than JavaBean and {@code record}
 * extractors &#8212; respectively through a {@link MapExtractorFactory} and a
 * {@link BeanExtractorFactory}.
 *
 * @param <T> the type of the beans produced by the {@code BeanExtractor}
 * @author Ayco Holleman
 * @see BeanExtractorFactory
 * @see MapExtractor
 */
public sealed interface BeanExtractor<T> extends Iterable<T> permits DefaultBeanExtractor,
      MapExtractor,
      RecordExtractor,
      CustomExtractor,
      NoopBeanExtractor {

  /**
   * Converts the current row within the {@code ResultSet} into a bean. If the
   * {@code ResultSet} is empty, or if there are no more rows in the {@code ResultSet}, an
   * empty {@code Optional} is returned. You can keep calling {@code extract()} to
   * successively extract all rows in the result set until you receive an empty
   * {@code Optional}, or until {@link #isEmpty()} returns {@code true}.
   *
   * @return an {@code Optional} containing the bean or an empty {@code Optional} if the
   *       {@code ResultSet} contained no (more) rows
   */
  Optional<T> extract();

  /**
   * Converts at most {@code limit} rows from the {@code ResultSet} into beans, possibly
   * less, if there are not that many rows left in the {@code ResultSet}. If the
   * {@code ResultSet} is empty, or if there are no more rows in the {@code ResultSet}, an
   * empty {@code List} is returned.
   *
   * @param limit the maximum number of rows to extract and convert
   * @return a {@code List} of beans or an empty {@code List} if the {@code ResultSet}
   *       contained no (more) rows
   */
  List<T> extract(int limit);

  /**
   * Converts all remaining rows in the {@code ResultSet} into beans. Equivalent to
   * {@link #extractAll(int) extractAll(10)}.
   *
   * @return a {@code List} of beans or an empty {@code List} if the {@code ResultSet}
   *       contained no (more) rows
   */
  default List<T> extractAll() { return extractAll(10); }

  /**
   * Converts all remaining rows in the {@code ResultSet} into beans.
   *
   * @param sizeEstimate an estimate of the size of the resulting {@code List}.
   * @return a {@code List} of beans or an empty {@code List} if the {@code ResultSet}
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
