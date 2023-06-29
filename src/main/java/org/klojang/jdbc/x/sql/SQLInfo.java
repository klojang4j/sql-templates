package org.klojang.jdbc.x.sql;

import org.klojang.util.collection.IntList;

import java.util.List;
import java.util.Map;

public final class SQLInfo {

  private final String unparsedSQL;
  private final String normalizedSQL;
  private final String jdbcSQL;
  private final List<NamedParameter> parameters;
  private final Map<String, IntList> parameterPositions;

  SQLInfo(String unparsedSQL, SQLNormalizer normalizer) {
    this.unparsedSQL = unparsedSQL;
    this.normalizedSQL = normalizer.getNormalizedSQL();
    this.jdbcSQL = normalizer.getNormalizedSQL();
    this.parameters = normalizer.getParameters();
    this.parameterPositions = normalizer.getParameterPositions();
  }

  SQLInfo(String unparsedSQL, String jdbcSQL, SQLNormalizer normalizer) {
    this.unparsedSQL = unparsedSQL;
    this.normalizedSQL = normalizer.getNormalizedSQL();
    this.jdbcSQL = jdbcSQL;
    this.parameters = normalizer.getParameters();
    this.parameterPositions = normalizer.getParameterPositions();
  }

  public String unparsedSQL() {return unparsedSQL;}

  public String normalizedSQL() {return normalizedSQL;}

  public String jdbcSQL() {return jdbcSQL;}

  public List<NamedParameter> parameters() {return parameters;}

  public Map<String, IntList> parameterPositions() {return parameterPositions;}

  @Override
  public String toString() {return unparsedSQL;}

}
