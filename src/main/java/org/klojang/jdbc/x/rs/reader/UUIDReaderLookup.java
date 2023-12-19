package org.klojang.jdbc.x.rs.reader;

import org.klojang.jdbc.x.rs.ColumnReader;
import org.klojang.jdbc.x.rs.ColumnReaderLookup;
import org.klojang.jdbc.x.rs.ResultSetMethod;

import java.util.UUID;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.rs.ResultSetMethod.GET_STRING;

public final class UUIDReaderLookup extends ColumnReaderLookup<UUID> {

  public UUIDReaderLookup() {
    add(OTHER, new ColumnReader<>(ResultSetMethod.objectGetter(UUID.class)));
    addMultiple(new ColumnReader<>(GET_STRING, UUID::fromString), CHAR, VARCHAR);
  }
}
