package org.klojang.jdbc.x;

import org.klojang.check.Check;
import org.klojang.jdbc.SQL;
import org.klojang.jdbc.SessionConfig;
import org.klojang.util.IOMethods;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.klojang.check.Tag.CLASS;
import static org.klojang.check.Tag.PATH;
import static org.klojang.jdbc.x.Strings.CONFIG;

/**
 * Cache of {@link SQL} objects created using from a classpath resource. Used to minimize
 * file i/o.
 */
public final class SQLCache {

  private record Key(String path, Class<?> clazz, SessionConfig config) {
    private static Key of(String path, Class<?> clazz, SessionConfig config) {
      return new Key(path, clazz, config);
    }

    private static Key of(String path, Class<?> clazz) {
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
    Check.notNull(clazz, CLASS);
    Check.notNull(path, PATH);
    Check.notNull(config, CONFIG);
    return cache.computeIfAbsent(Key.of(path, clazz, config), k -> {
      try {
        String sql = IOMethods.getContents(clazz, path);
        return factory.apply(config, sql);
      } catch (Throwable t) {
        throw Utils.wrap(t);
      }
    });
  }
}
