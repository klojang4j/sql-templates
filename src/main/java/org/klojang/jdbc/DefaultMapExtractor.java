package org.klojang.jdbc;

import org.klojang.jdbc.x.rs.KeyWriter;

import java.sql.ResultSet;
import java.util.Map;

import static org.klojang.jdbc.x.rs.KeyWriter.toMap;

final class DefaultMapExtractor extends AbstractBeanExtractor<Map<String, Object>>
      implements MapExtractor {

  DefaultMapExtractor(ResultSet rs, KeyWriter<?>[] writers) {
    super(rs, x -> toMap(x, writers));
  }

}
