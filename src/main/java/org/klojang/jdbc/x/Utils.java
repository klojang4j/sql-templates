package org.klojang.jdbc.x;

import org.klojang.check.Check;
import org.klojang.check.ObjectCheck;
import org.klojang.jdbc.KlojangSQLException;

public final class Utils {

  private Utils() { throw new UnsupportedOperationException(); }

  public static <T> ObjectCheck<T, KlojangSQLException> check(T arg) {
    return Check.on(KlojangSQLException::new, arg);
  }
}
