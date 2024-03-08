package org.klojang.jdbc;

import java.sql.ResultSet;

/**
 * Specifies the capacities of {@link BeanExtractor} factory objects.
 *
 * @see BatchQuery#BatchQuery(BatchQuery.QueryId, ExtractorFactory) 
 *
 * @param <T> the type of the objects produced by the {@code BeanExtractor}
 */
public sealed interface ExtractorFactory<T>
      permits BeanExtractorFactory, MapExtractorFactory {

  /**
   * Returns a {@code BeanExtractor} that will convert the rows in the specified
   * {@code ResultSet} into objects of type {@code <T>}.
   *
   * @param rs the {@code ResultSet}
   * @return a {@code BeanExtractor} that will convert the rows in the specified
   *       {@code ResultSet} into objects of type {@code <T>}
   */
  BeanExtractor<T> getExtractor(ResultSet rs);

}
