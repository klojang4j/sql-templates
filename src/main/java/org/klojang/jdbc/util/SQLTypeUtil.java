package org.klojang.jdbc.util;

import org.klojang.check.Check;
import org.klojang.jdbc.x.Utils;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.sql.Types;
import java.util.Map;
import java.util.TreeMap;

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

  /**
   * Returns the name of the constant with the specified value. Throws an
   * {@link IllegalArgumentException} if there is no constant with the specified value in
   * the {@link Types} class.
   *
   * @param sqlType the value of the constant whose name you want to retrieve
   * @return the name of the constant with the specified value
   */
  public static String getTypeName(int sqlType) {
    Check.that(sqlType).is(SQLTypeUtil::isValidType, NO_SUCH_TYPE);
    return cache.get(sqlType);
  }

  /**
   * Returns {@code true} if the specified integer is the value of one of the constants in
   * the {@link Types} class, {@code false} otherwise.
   *
   * @param sqlType the integer value to check
   * @return {@code true} if the integer is the value of one of the constants in the
   *       {@code Types} class, {@code false} otherwise
   */
  public static boolean isValidType(int sqlType) {
    return cache.containsKey(sqlType);
  }

  /**
   * Returns the value of the constant with the specified name. Throws an
   * {@link IllegalArgumentException} if there is no constant with the specified name in
   * the {@link Types} class.
   *
   * @param name the name of the constant (e&#46;g&#46; "VARCHAR" or "TIMESTAMP")
   * @return the value of the constant with the specified name
   */
  public static int forName(String name) {
    return Check.that(name).is(SQLTypeUtil::isValidName, NO_SUCH_TYPE).ok(reverse::get);
  }

  /**
   * Returns {@code true} if the specified string is the name of one of the constants in
   * the {@link Types} class, {@code false} otherwise.
   *
   * @param name the string value to check
   * @return {@code true} if the specified string is the name of one of the constants in
   *       the {@code Types} class, {@code false} otherwise
   */
  public static boolean isValidName(String name) {
    return reverse.containsKey(name);
  }

  /**
   * Returns the values of all constants in ascending order.
   *
   * @return the values of all constants in ascending order
   */
  public static int[] getAllTypes() {
    return cache.keySet().stream().mapToInt(Integer::intValue).toArray();
  }

  /**
   * Returns the names of all constants, sorted alphabetically.
   *
   * @return the names of all constants, sorted alphabetically
   */
  public static String[] getAllTypeNames() {
    return reverse.keySet().toArray(String[]::new);
  }

  /**
   * Prints the values and names of all constants in ascending order of the values of the
   * constants.
   *
   * @param out the {@code PrintStream} to write to
   */
  public static void printAll(PrintStream out) {
    Check.notNull(out);
    cache.forEach((k, v) -> out.printf("%5d : %s%n", k, v));
  }

  /**
   * Prints the names and values of all constants, sorted alphabetically on the names of
   * the constants.
   *
   * @param out the {@code PrintStream} to write to
   */
  public static void printAllNames(PrintStream out) {
    Check.notNull(out);
    reverse.forEach((k, v) -> out.printf("%23s : %d%n", k, v));
  }

  private static final Map<Integer, String> cache;
  private static final Map<String, Integer> reverse;

  static {
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
