package org.klojang.jdbc.x.ps;

@FunctionalInterface
public interface Adapter<FIELD_TYPE, PARAM_TYPE> {

  PARAM_TYPE adapt(FIELD_TYPE value, Class<PARAM_TYPE> targetType);
}
