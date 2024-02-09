package org.klojang.jdbc.x.rs.reader;

import org.klojang.jdbc.x.rs.ColumnReader;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static java.sql.Types.*;
import static java.util.Map.Entry;
import static org.klojang.jdbc.x.rs.ResultSetMethod.*;

public final class UUIDReaderLookup extends AbstractColumnReaderLookup<UUID> {

  @Override
  List<Entry<Integer, ColumnReader<?, UUID>>> getColumnReaders() {
    List<Entry<Integer, ColumnReader<?, UUID>>> entries = new ArrayList<>();
    entries.add(entry(objectGetter(UUID.class), OTHER));
    entries.addAll(entries(GET_STRING, stringToUUID(), VARCHAR, CHAR, BINARY));
    return entries;
  }

  private static Function<String, UUID> stringToUUID() {
    return UUID::fromString;
  }
}
