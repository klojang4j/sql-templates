package org.klojang.jdbc.x.ps.writer;

import org.klojang.jdbc.x.ps.ValueBinder;
import org.klojang.jdbc.x.ps.ValueBinderLookup;

import static java.sql.Types.*;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.SET_BYTES;

public final class ByteArrayBinderLookup extends ValueBinderLookup<byte[]> {

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static final ValueBinder DEFAULT = new ValueBinder(SET_BYTES);

  @SuppressWarnings("unchecked")
  public ByteArrayBinderLookup() {
    addMultiple(DEFAULT, VARBINARY, BINARY, LONGVARBINARY);
  }

}
