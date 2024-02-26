package org.klojang.jdbc.x.rs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;

import org.klojang.jdbc.CustomReader;

/**
 * Reads a single column in a ResultSet as the value for a record component.
 *
 * @param <COLUMN_TYPE>
 * @param <COMPONENT_TYPE>
 */
public final class ComponentWriter<COLUMN_TYPE, COMPONENT_TYPE> {

  private static final Logger LOG = LoggerFactory.getLogger(ComponentWriter.class);

  private final ColumnReader<COLUMN_TYPE, COMPONENT_TYPE> reader;
  private final String component;
  private final int columnIndex;
  private final Class<COMPONENT_TYPE> componentType;
  private final CustomReader custom;

  ComponentWriter(
        ColumnReader<COLUMN_TYPE, COMPONENT_TYPE> reader,
        String component,
        int columnIndex,
        Class<COMPONENT_TYPE> componentType) {
    this.reader = reader;
    this.component = component;
    this.columnIndex = columnIndex;
    this.componentType = componentType;
    this.custom = null;
  }

  ComponentWriter(CustomReader custom, String component, int columnIndex) {
    this.custom = custom;
    this.component = component;
    this.columnIndex = columnIndex;
    this.componentType = null;
    this.reader = null;
  }

  @SuppressWarnings("unchecked")
  COMPONENT_TYPE readValue(ResultSet resultset) throws Throwable {
    COMPONENT_TYPE val;
    if (custom == null) {
      val = reader.getValue(resultset, columnIndex, componentType);
      LOG.trace("==> {}: {}", component, val);
    } else {
      val = (COMPONENT_TYPE) custom.getValue(resultset, columnIndex);
      LOG.trace("==> {}: {} (using custom reader)", component, val);
    }
    return val;
  }
}
