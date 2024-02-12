package org.klojang.jdbc.x;

import org.klojang.check.Check;
import org.klojang.jdbc.SessionConfig;
import org.klojang.jdbc.SQL;
import org.klojang.util.IOMethods;
import org.klojang.util.Tuple2;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.klojang.check.Tag.CLASS;
import static org.klojang.check.Tag.PATH;

public final class SQLCache {

  private static final Map<Tuple2<Class<?>, String>, SQL> cache = new HashMap<>();

  public static SQL get(Class<?> clazz, String path, Function<String, SQL> factory) {
    Check.notNull(clazz, CLASS);
    Check.notNull(path, PATH);
    return cache.computeIfAbsent(Tuple2.of(clazz, path), k -> {
      try {
        String sql = IOMethods.getContents(clazz, path);
        return factory.apply(sql);
      } catch (Throwable t) {
        throw Utils.wrap(t);
      }
    });
  }

  public static SQL get(Class<?> clazz,
        String path,
        SessionConfig config,
        BiFunction<String, SessionConfig, SQL> factory) {
    return cache.computeIfAbsent(Tuple2.of(clazz, path), k -> {
      try {
        String sql = IOMethods.getContents(clazz, path);
        return factory.apply(sql, config);
      } catch (Throwable t) {
        throw Utils.wrap(t);
      }
    });
  }
}
