package org.klojang.jdbc.x.rs;

import org.klojang.jdbc.x.rs.reader.AbstractColumnReaderLookup;
import org.klojang.jdbc.x.rs.reader.StringReaderLookup;

public sealed interface ColumnReaderLookup<T> permits AbstractColumnReaderLookup,
      StringReaderLookup {

  ColumnReader<?, T> getColumnReader(int columnType);

}
