package org.klojang.jdbc.x.rs;

import java.sql.ResultSet;

final class RecordChannel<COLUMN_TYPE, COMPONENT_TYPE> {

  private final ColumnReader<COLUMN_TYPE, COMPONENT_TYPE> reader;
  private final int columnIndex;
  private final Class<COMPONENT_TYPE> componentType;

  RecordChannel(
        ColumnReader<COLUMN_TYPE, COMPONENT_TYPE> reader,
        int columnIndex,
        Class<COMPONENT_TYPE> componentType) {
    this.reader = reader;
    this.columnIndex = columnIndex;
    this.componentType = componentType;
  }

  COMPONENT_TYPE readValue(ResultSet resultset) throws Throwable {
    return reader.getValue(resultset, columnIndex, componentType);
  }
}
