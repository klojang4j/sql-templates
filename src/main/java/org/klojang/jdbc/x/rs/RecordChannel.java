package org.klojang.jdbc.x.rs;

import java.sql.ResultSet;

final class RecordChannel<COLUMN_TYPE, COMPONENT_TYPE> {

  private final ColumnReader<COLUMN_TYPE, COMPONENT_TYPE> reader;
  private final Class<COMPONENT_TYPE> componentType;
  private final int jdbcIdx;

  RecordChannel(
        ColumnReader<COLUMN_TYPE, COMPONENT_TYPE> reader,
        Class<COMPONENT_TYPE> componentType,
        int jdbcIdx) {
    this.reader = reader;
    this.componentType = componentType;
    this.jdbcIdx = jdbcIdx;
  }

  COMPONENT_TYPE readValue(ResultSet rs) throws Throwable {
    return reader.getValue(rs, jdbcIdx, componentType);
  }
}
