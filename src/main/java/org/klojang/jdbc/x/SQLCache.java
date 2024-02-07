package org.klojang.jdbc.x;

import org.klojang.jdbc.BindInfo;
import org.klojang.jdbc.SQL;
import org.klojang.util.IOMethods;
import org.klojang.util.Tuple2;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class SQLCache {

  private static final Map<Tuple2<Class<?>, String>, SQL> cache = new HashMap<>();

  public static SQL get(Class<?> clazz, String path, Function<String, SQL> factory) {
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
        BindInfo bindInfo,
        BiFunction<String, BindInfo, SQL> factory) {
    return cache.computeIfAbsent(Tuple2.of(clazz, path), k -> {
      try {
        String sql = IOMethods.getContents(clazz, path);
        return factory.apply(sql, bindInfo);
      } catch (Throwable t) {
        throw Utils.wrap(t);
      }
    });
  }
}
