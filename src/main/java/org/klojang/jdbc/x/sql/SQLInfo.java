package org.klojang.jdbc.x.sql;

import org.klojang.util.collection.IntList;

import java.util.List;
import java.util.Map;

public record SQLInfo(
      String unparsedSQL,
      String normalizedSQL,
      String jdbcSQL,
      List<NamedParameter> parameters,
      Map<String, IntList> parameterPositions) {

  SQLInfo(String unparsedSQL, SQLNormalizer normalizer) {
    this(unparsedSQL,
          normalizer.getNormalizedSQL(),
          normalizer.getNormalizedSQL(),
          normalizer.getParameters(),
          normalizer.getParameterPositions());
  }

  SQLInfo(String unparsedSQL, String jdbcSQL, SQLNormalizer normalizer) {
    this(unparsedSQL,
          normalizer.getNormalizedSQL(),
          jdbcSQL,
          normalizer.getParameters(),
          normalizer.getParameterPositions());
  }
}
