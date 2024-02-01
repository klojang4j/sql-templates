package org.klojang.jdbc.x.sql;

import org.klojang.util.collection.IntList;

import java.util.List;
import java.util.Map;

public record SQLInfo(String normalizedSQL,
      String jdbcSQL,
      List<NamedParameter> parameters,
      Map<String, IntList> parameterPositions) {

  public SQLInfo(ParamExtractor paramExtractor) {
    this(paramExtractor.getNormalizedSQL(),
          paramExtractor.getNormalizedSQL(),
          paramExtractor.getParameters(),
          paramExtractor.getParameterPositions());
  }

  public SQLInfo(String jdbcSQL, ParamExtractor paramExtractor) {
    this(paramExtractor.getNormalizedSQL(),
          jdbcSQL,
          paramExtractor.getParameters(),
          paramExtractor.getParameterPositions());
  }


}
