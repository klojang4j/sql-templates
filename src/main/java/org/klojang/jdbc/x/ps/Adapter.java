package org.klojang.jdbc.x.ps;

/**
 * Converts a value such that it is type-compatible with the particular {@code setXXX()}
 * method of {@code PreparedStatement} that we want to pass the value to. Implementations
 * do **not** need to build in null-resistance. The value passed to the
 * {@link #adapt(Object, Class) adapt()} method is guaranteed never to be {@code null}.
 *
 * @param <INPUT_TYPE> the type of the input value
 * @param <PARAM_TYPE> the type required by the {@code setXXX()} we want to call
 */
@FunctionalInterface
public interface Adapter<INPUT_TYPE, PARAM_TYPE> {

  PARAM_TYPE adapt(INPUT_TYPE value, Class<PARAM_TYPE> targetType);
}
