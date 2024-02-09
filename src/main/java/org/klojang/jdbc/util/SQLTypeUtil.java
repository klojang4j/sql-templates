package org.klojang.jdbc.util;

import org.klojang.check.Check;
import org.klojang.jdbc.x.Utils;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.sql.Types;
import java.util.Map;
import java.util.TreeMap;

import static org.klojang.check.CommonChecks.keyIn;

/**
 * Utility class for translating between the names and values of the constants in the
 * {@link Types java.sql.Types} class. The {@code Types} class screams out to be an
 * {@code enum} type, but this class predates the introduction of enums into the Java
 * language. There are quite a lot of {@code int} constants in the {@code Types}, so it is
 * hard to remember what type you are dealing with when all you know is that is symbolized
 * by, say, {@code int} value {@code 12} (for the curious: that is the value of
 * {@link Types#VARCHAR VARCHAR} constant).
 */
public final class SQLTypeUtil {

  private static final String NO_SUCH_TYPE = "no such constant in java.sql.Types: ${arg}";

  private static final SQLTypeUtil instance = new SQLTypeUtil();

  /**
   * Returns the name of the constant with the specified value.
   *
   * @param sqlType the value of the constant whose name you want to retrieve
   * @return the name of the constant with the specified value
   */
  public static String getTypeName(int sqlType) {
    Check.that((Integer) sqlType).is(keyIn(), instance.cache, NO_SUCH_TYPE);
    return instance.cache.get(sqlType);
  }

  /**
   * Returns the value of the constant with the specified name.
   *
   * @param name the name of the constant (e&#46;g&#46; "VARCHAR" or "TIMESTAMP")
   * @return the value of the constant with the specified name
   */
  public static int forName(String name) {
    Check.that(name).is(keyIn(), instance.reverse, NO_SUCH_TYPE);
    return instance.reverse.get(name);
  }

  /**
   * Returns the values of all constants in ascending order.
   *
   * @return the values of all constants in ascending order
   */
  public static int[] getAllTypes() {
    return instance.cache.keySet().stream().mapToInt(Integer::intValue).toArray();
  }

  /**
   * Returns the names of all constants, sorted alphabetically.
   *
   * @return the names of all constants, sorted alphabetically
   */
  public static String[] getAllTypeName() {
    return instance.reverse.keySet().toArray(String[]::new);
  }

  /**
   * Prints the values and names of all constants.
   *
   * @param out the {@code PrintStream} to write to
   */
  public static void printAll(PrintStream out) {
    Check.notNull(out);
    instance.cache.forEach((k, v) -> out.printf("%5d : %s%n", k, v));
  }

  private final Map<Integer, String> cache;
  private final Map<String, Integer> reverse;

  private SQLTypeUtil() {
    Class<Types> clazz = Types.class;
    Field[] fields = clazz.getDeclaredFields();
    cache = new TreeMap<>();
    reverse = new TreeMap<>();
    try {
      for (Field f : fields) {
        int type = f.getInt(null);
        cache.put(type, f.getName());
        reverse.put(f.getName(), type);
      }
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }
}
