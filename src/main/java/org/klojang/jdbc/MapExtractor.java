package org.klojang.jdbc;

import java.sql.ResultSet;
import java.util.Map;

/**
 * <p>Converts the rows in a JDBC {@link ResultSet} into {@code Map<String, Object>}
 * pseudo-objects. Instances are obtained via
 * {@link MapExtractorFactory#getExtractor(ResultSet) MapExtractorFactory.getExtractor()}.
 *
 * @author Ayco Holleman
 * @see MapExtractorFactory
 * @see BeanExtractor
 */
public sealed interface MapExtractor extends BeanExtractor<Map<String, Object>>
      permits DefaultMapExtractor, NoopMapExtractor {

}
