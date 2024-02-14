package org.klojang.jdbc;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>Converts the rows in a JDBC {@link ResultSet} into {@code Map<String, Object>}
 * pseudo-objects. Instances are obtained via
 * {@link MapExtractorFactory#getExtractor(ResultSet) MapExtractorFactory.getExtractor()}.
 *
 * @author Ayco Holleman
 * @see MapExtractorFactory
 * @see BeanExtractor
 */
public sealed interface MapExtractor extends Iterable<Map<String, Object>>
      permits DefaultMapExtractor, NoopMapExtractor {

  /**
   * Converts the current row in the {@code ResultSet} into a map. If the
   * {@code ResultSet} is empty, or if there are no more rows in the {@code ResultSet}, an
   * empty {@code Optional} is returned. You can keep calling {@code extract()} to
   * successively extract all rows in the result set until you receive an empty
   * {@code Optional}, or until {@link #isEmpty()} returns {@code true}.
   *
   * @return a map containing the values in the current row in a {@code ResultSet}
   * @see MapExtractorFactory#getExtractor(ResultSet)
   */
  Optional<Map<String, Object>> extract();

  /**
   * Converts at most {@code limit} rows in the {@code ResultSet} into maps, possibly
   * less, if there are not that many rows left in the {@code ResultSet}. If the
   * {@code ResultSet} is empty, or if there are no more rows in the {@code ResultSet}, an
   * empty {@code List} is returned.
   *
   * @param limit the maximum number of records to extract and convert
   * @return a {@code List} of {@code Map} objects or an empty {@code List} if the
   *       {@code ResultSet} contained no (more) rows
   */
  List<Map<String, Object>> extract(int limit);

  /**
   * Converts all remaining rows in the {@code ResultSet} into maps.
   *
   * @return a {@code List} of {@code Map} objects or an empty {@code List} if the
   *       {@code ResultSet} contained no (more) rows
   */
  List<Map<String, Object>> extractAll();

  /**
   * Converts all remaining rows in the {@code ResultSet} into maps.
   *
   * @param sizeEstimate an estimate of the number of remaining rows in the
   *       {@code ResultSet}
   * @return a {@code List} of {@code Map} objects or an empty {@code List} if the
   *       {@code ResultSet} contained no (more) rows
   */
  List<Map<String, Object>> extractAll(int sizeEstimate);

  /**
   * Returns {@code true} if the end of the {@code ResultSet} has been reached;
   * {@code false} otherwise.
   *
   * @return whether the end of the {@code ResultSet} has been reached
   */
  boolean isEmpty();

}
