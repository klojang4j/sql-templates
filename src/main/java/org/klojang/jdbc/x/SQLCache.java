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

  private record Key(String path, Class<?> clazz, SessionConfig config) {
    static Key of(String path, Class<?> clazz, SessionConfig config) {
      return new Key(path, clazz, config);
    }

    static Key of(String path, Class<?> clazz) {
      return new Key(path, clazz, Utils.DEFAULT_CONFIG);
    }

  }

  private static final Map<Key, SQL> cache = new HashMap<>();

  public static SQL get(Class<?> clazz, String path, Function<String, SQL> factory) {
    Check.notNull(clazz, CLASS);
    Check.notNull(path, PATH);
    return cache.computeIfAbsent(Key.of(path, clazz), k -> {
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
        BiFunction<SessionConfig, String, SQL> factory) {
    return cache.computeIfAbsent(Key.of(path, clazz), k -> {
      try {
        String sql = IOMethods.getContents(clazz, path);
        return factory.apply(config, sql);
      } catch (Throwable t) {
        throw Utils.wrap(t);
      }
    });
  }
}
