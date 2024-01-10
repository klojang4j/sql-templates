package org.klojang.jdbc.x.rs;

import java.sql.ResultSet;

/**
 * Reads a single column in a ResultSet as the value for a record component.
 *
 * @param <COLUMN_TYPE>
 * @param <COMPONENT_TYPE>
 */
final class ComponentWriter<COLUMN_TYPE, COMPONENT_TYPE> {

  private final ColumnReader<COLUMN_TYPE, COMPONENT_TYPE> reader;
  private final int columnIndex;
  private final Class<COMPONENT_TYPE> componentType;

  ComponentWriter(
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
