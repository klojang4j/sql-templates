package org.klojang.jdbc.x.rs.reader;

import org.klojang.convert.Bool;
import org.klojang.jdbc.x.rs.ColumnReader;

import java.util.ArrayList;
import java.util.List;

import static java.sql.Types.*;
import static java.util.Map.Entry;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;


public final class BooleanReaderLookup extends AbstractColumnReaderLookup<Boolean> {

  @Override
  List<Entry<Integer, ColumnReader<?, Boolean>>> getColumnReaders() {
    List<Entry<Integer, ColumnReader<?, Boolean>>> entries = new ArrayList<>(16);
    entries.addAll(entries(GET_BOOLEAN, BOOLEAN));
    entries.add(entry(GET_LONG, Bool::from, BIGINT));
    entries.add(entry(GET_FLOAT, Bool::from, REAL));
    entries.addAll(entries(GET_INT, Bool::from, INTEGER, SMALLINT, TINYINT));
    entries.addAll(entries(GET_DOUBLE, Bool::from, FLOAT, DOUBLE));
    entries.addAll(entries(GET_BIG_DECIMAL, Bool::from, NUMERIC, DECIMAL));
    entries.addAll(entries(GET_STRING, Bool::from, VARCHAR, CHAR));
    return entries;
  }

}
