package org.klojang.jdbc;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

/**
 * <p>Converts the rows in a JDBC {@link ResultSet} into JavaBeans or records. Instances
 * are obtained via
 * {@link BeanifierFactory#getBeanifier(ResultSet) BeanifierFactory.getBeanifier()}.
 *
 * <p>Contrary to the {@link SQLQuery} class, a
 * {@code ResultSetBeanifier} is agnostic about, and has no opinion on where you got the
 * {@link ResultSet} from. In that sense it is closer to the JDBC metal than the
 * {@code SQLQuery} class. It certainly does not pretend to be an ORM-like class. It just
 * converts result sets into (flat) JavaBeans, to be carried across the boundary of the
 * data access module (and into the view layer). The JavaBeans may have nested structures,
 * but only top-level properties will be populated.
 *
 * <p>{@code ResultSetBeanifier} is an extension of the {@link Iterable} interface,
 * enabling you to conveniently extract the JavaBeans in a {@code forEach} loop:
 *
 * <blockquote><pre>{@code
 * ResultSet rs = ...;
 * BeanifierFactory factory = new BeanifierFactory(Employee.class);
 * for(Employee emp : factory.getBeanifier(rs)) {
 *   // do stuff ...
 * }
 * }</pre></blockquote>
 *
 * <h2>JavaBeans vs. Records</h2>
 * When converting a row to a JavaBean, a {@code ResultSetBeanifier} will always use the
 * setters on the JavaBean to populate it. There is no way to populate the bean via its
 * constructors. When converting to a record, a {@code ResultSetBeanifier} will obviously
 * always use one of its constructors to populate it (since records don't have setters).
 * Make sure the record has a constructor for those record components that map to columns
 * in the SELECT clause. Also, the encounter order of the record components within the
 * constructor must match the encounter order of the corresponding columns in the SELECT
 * clause. They don't need to pair-wise line up, though. Not all record components must be
 * mappable to columns in the SELECT clause and not columns in the SELECT clause must be
 * mappable to record components.
 *
 * @param <T> the type of the JavaBeans produced by the {@code ResultSetBeanifier}
 * @author Ayco Holleman
 * @see BeanifierFactory
 * @see ResultSetMappifier
 */
public sealed interface ResultSetBeanifier<T> extends Iterable<T>
      permits DefaultBeanifier, EmptyBeanifier, RecordBeanifier {

  /**
   * Converts the current row within the {@code ResultSet} into a JavaBean. If the
   * {@code ResultSet} is empty, or if there are no more rows in the {@code ResultSet}, an
   * empty {@code Optional} is returned. You can keep calling {@code beanify()} to
   * successively extract all rows in the result set.
   *
   * @return an {@code Optional} containing the JavaBean or an empty {@code Optional} if
   * the {@code ResultSet} contained no (more) rows
   */
  Optional<T> beanify();

  /**
   * Converts at most {@code limit} rows from the {@code ResultSet} into JavaBeans. If the
   * {@code ResultSet} is empty, an empty {@code List} is returned.
   *
   * @param limit the maximum number of rows to extract and convert
   * @return a {@code List} of JavaBeans or an empty {@code List} if the {@code ResultSet}
   * contained no (more) rows
   */
  List<T> beanify(int limit);

  /**
   * Converts all remaining rows in the {@code ResultSet} into JavaBeans.
   *
   * @return a {@code List} of JavaBeans or an empty {@code List} if the {@code ResultSet}
   * contained no (more) rows
   */
  List<T> beanifyAll();

  /**
   * Converts all remaining rows in the {@code ResultSet} into JavaBeans.
   *
   * @param sizeEstimate an estimate of the size of the resulting {@code List}.
   * @return a {@code List} of JavaBeans or an empty {@code List} if the {@code ResultSet}
   * contained no (more) rows
   */
  List<T> beanifyAll(int sizeEstimate);

  /**
   * Returns {@code true} if the end of the {@code ResultSet} has been reached;
   * {@code false} otherwise.
   *
   * @return whether the end of the {@code ResultSet} has been reached
   */
  boolean isEmpty();

}
