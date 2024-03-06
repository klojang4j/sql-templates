package org.klojang.jdbc;

import org.klojang.jdbc.x.rs.PropertyWriter;

import java.sql.ResultSet;
import java.util.function.Supplier;

import static org.klojang.jdbc.x.rs.PropertyWriter.writeAll;

final class DefaultBeanExtractor<T> extends AbstractBeanExtractor<T> {

  DefaultBeanExtractor(ResultSet rs,
        PropertyWriter<?, ?>[] writers,
        Supplier<T> supplier) {
    super(rs, x -> writeAll(x, supplier, writers));
  }


}
