package org.klojang.jdbc;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>Converts the rows in a JDBC {@link ResultSet} into {@code Map<String, Object} pseudo-objects. Instances are
 * obtained via
 * {@link MappifierFactory#getMappifier(ResultSet)}  MappifierFactory.getMappifier()}.
 * {@code ResultSetBeanifier} is an extension of the {@link Iterable} interface, enabling
 * you to conveniently extract the JavaBeans in a {@code forEach} loop:
 *
 * <blockquote><pre>{@code
 * ResultSet rs = ...;
 * BeanifierFactory factory = new BeanifierFactory(Employee.class);
 * for(Employee emp : factory.getBeanifier(rs)) {
 *   // do stuff ...
 * }
 * }</pre></blockquote>
 *
 * @author Ayco Holleman
 */
public sealed interface ResultSetMappifier extends Iterable<Map<String, Object>>
      permits DefaultMappifier, EmptyMappifier {

  /**
   * Converts the current row in a {@code ResultSet} into a map. If the {@code ResultSet}
   * is empty, or if there are no more rows in the {@code ResultSet}, an empty
   * {@code Optional} is returned.
   *
   * @return a map containing the values in the current row in a {@code ResultSet}
   * @see MappifierFactory#getMappifier(ResultSet)
   */
  Optional<Map<String, Object>> mappify();

  /**
   * Converts at most {@code limit} rows in a {@code ResultSet} into maps. If the
   * {@code ResultSet} is empty, an empty {@code List} is returned.
   *
   * @param limit the maximum number of records to mappify
   * @return a {@code List} of {@code Map} objects or an empty {@code List} if the
   * {@code ResultSet} contained no (more) rows
   */
  List<Map<String, Object>> mappify(int limit);

  /**
   * Converts all remaining rows in a {@code ResultSet} into maps.
   *
   * @return a {@code List} of {@code Map} objects or an empty {@code List} if the
   * {@code ResultSet} contained no (more) rows
   */
  List<Map<String, Object>> mappifyAll();

  /**
   * Converts all remaining rows in a {@code ResultSet} into maps.
   *
   * @param sizeEstimate an estimate of the total size of the result set
   * @return a {@code List} of {@code Map} objects or an empty {@code List} if the
   * {@code ResultSet} contained no (more) rows
   */
  List<Map<String, Object>> mappifyAll(int sizeEstimate);

  /**
   * Returns {@code true} if the end of the {@code ResultSet} has been reached;
   * {@code false} otherwise.
   *
   * @return Whether the end of the {@code ResultSet} has been reached
   */
  boolean isEmpty();

}
