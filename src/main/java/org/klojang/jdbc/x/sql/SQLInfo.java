package org.klojang.jdbc.x.sql;

import org.klojang.util.collection.IntList;

import java.util.List;
import java.util.Map;

public record SQLInfo(String sql,
      List<NamedParameter> parameters,
      Map<String, IntList> parameterPositions) {

  public SQLInfo(ParamExtractor paramExtractor) {
    this(paramExtractor.getNormalizedSQL(),
          paramExtractor.getParameters(),
          paramExtractor.getParameterPositions());
  }

  public SQLInfo(String sql, ParamExtractor paramExtractor) {
    this(sql, paramExtractor.getParameters(), paramExtractor.getParameterPositions());
  }


}
