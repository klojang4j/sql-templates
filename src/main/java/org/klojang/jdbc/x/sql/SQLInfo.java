package org.klojang.jdbc.x.sql;

import org.klojang.util.collection.IntList;

import java.util.List;
import java.util.Map;

public record SQLInfo(String normalizedSQL,
      String jdbcSQL,
      List<NamedParameter> parameters,
      Map<String, IntList> parameterPositions) {

  public SQLInfo(SQLNormalizer normalizer) {
    this(normalizer.getNormalizedSQL(),
          normalizer.getNormalizedSQL(),
          normalizer.getParameters(),
          normalizer.getParameterPositions());
  }

  public SQLInfo(String jdbcSQL, SQLNormalizer normalizer) {
    this(normalizer.getNormalizedSQL(),
          jdbcSQL,
          normalizer.getParameters(),
          normalizer.getParameterPositions());
  }


}
