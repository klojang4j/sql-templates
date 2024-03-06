package org.klojang.jdbc;

import org.klojang.check.fallible.FallibleFunction;

import java.sql.ResultSet;
import java.sql.SQLException;

final class CustomExtractor<T> extends AbstractBeanExtractor<T> {

  CustomExtractor(ResultSet rs, FallibleFunction<ResultSet, T, SQLException> converter) {
    super(rs, converter);
  }
}
