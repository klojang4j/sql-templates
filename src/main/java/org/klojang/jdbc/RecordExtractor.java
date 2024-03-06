package org.klojang.jdbc;

import org.klojang.jdbc.x.rs.RecordFactory;

import java.sql.ResultSet;

final class RecordExtractor<T extends Record> extends AbstractBeanExtractor<T> {

  RecordExtractor(ResultSet rs, RecordFactory<T> factory) {
    super(rs, x -> factory.createRecord(x));
  }

}
