package org.klojang.jdbc.x.sql;

import org.klojang.util.collection.IntList;

import java.util.List;
import java.util.Map;

public record ParameterInfo(String normalizedSQL,
      List<NamedParameter> parameters,
      Map<String, IntList> parameterPositions) {

  public ParameterInfo(ParamExtractor paramExtractor) {
    this(paramExtractor.getNormalizedSQL(),
          paramExtractor.getParameters(),
          paramExtractor.getParameterPositions());
  }

  public ParameterInfo(String sql, ParamExtractor paramExtractor) {
    this(sql, paramExtractor.getParameters(), paramExtractor.getParameterPositions());
  }

}
