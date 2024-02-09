package org.klojang.jdbc.x.rs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;

/**
 * Reads a single column in a ResultSet as the value for a record component.
 *
 * @param <COLUMN_TYPE>
 * @param <COMPONENT_TYPE>
 */
final class ComponentWriter<COLUMN_TYPE, COMPONENT_TYPE> {

  private static final Logger LOG = LoggerFactory.getLogger(ComponentWriter.class);


  private final ColumnReader<COLUMN_TYPE, COMPONENT_TYPE> reader;
  private final String component;
  private final int columnIndex;
  private final Class<COMPONENT_TYPE> componentType;

  ComponentWriter(
        ColumnReader<COLUMN_TYPE, COMPONENT_TYPE> reader,
        String component,
        int columnIndex,
        Class<COMPONENT_TYPE> componentType) {
    this.reader = reader;
    this.component = component;
    this.columnIndex = columnIndex;
    this.componentType = componentType;
  }

  COMPONENT_TYPE readValue(ResultSet resultset) throws Throwable {
    COMPONENT_TYPE val = reader.getValue(resultset, columnIndex, componentType);
    LOG.trace("==> {}: {}", component, val);
    return val;
  }
}
