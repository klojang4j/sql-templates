package org.klojang.jdbc.x.ps;

import org.klojang.jdbc.SessionConfig;
import org.klojang.jdbc.x.ps.writer.EnumBinderLookup;
import org.klojang.jdbc.x.sql.NamedParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.klojang.jdbc.CustomBinder;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.SET_BYTES;
import static org.klojang.jdbc.x.ps.PreparedStatementMethod.SET_STRING;
import static org.klojang.util.ClassMethods.isSubtype;

/**
 * Binds the values within in a Map to a PreparedStatement.
 */
public final class MapBinder {

  private static final Logger LOG = LoggerFactory.getLogger(MapBinder.class);

  private final List<NamedParameter> params;
  private final SessionConfig config;

  public MapBinder(List<NamedParameter> params, SessionConfig config) {
    this.params = params;
    this.config = config;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void bind(PreparedStatement stmt, Map<String, Object> map,
        Set<NamedParameter> bound) throws Throwable {
    for (NamedParameter param : params) {
      String key = param.name();
      if (!map.containsKey(key)) {
        continue;
      }
      bound.add(param);
      Object val = map.get(key);
      if (val == null) {
        LOG.trace("==> Parameter \"{}\": null", key);
        param.positions().forEachThrowing(i -> stmt.setNull(i, Types.OTHER));
        continue;
      }
      Class mapType = map.getClass();
      Class valType = val.getClass();
      CustomBinder cb = config.getCustomBinder(mapType, key, valType);
      if (cb != null) {
        LOG.trace("==> Parameter \"{}\": {} (using custom binder)", key, val);
        param.positions().forEachThrowing(i -> cb.bind(stmt, i, val));
        continue;
      }
      Integer sqlType = config.getSQLType(mapType, key, valType);
      if (sqlType != null) {
        ValueBinderFactory factory = ValueBinderFactory.getInstance();
        ValueBinder vb = factory.getBinder(valType, sqlType);
        bind(stmt, param, vb, val);
        continue;
      }
      if (isSubtype(valType, Enum.class)) {
        ValueBinder vb = config.saveEnumAsString(mapType, key, valType)
              ? ValueBinder.ANY_TO_STRING
              : EnumBinderLookup.DEFAULT;
        bind(stmt, param, vb, val);
        continue;
      }
      if (isSubtype(valType, TemporalAccessor.class)) {
        DateTimeFormatter dtf = config.getDateTimeFormatter(mapType, key, valType);
        if (dtf != null) {
          ValueBinder vb = ValueBinder.dateTimeToString(dtf);
          bind(stmt, param, vb, val);
          continue;
        }
      }
      Function<Object, String> ser0 = config.getSerializer(mapType, key, valType);
      if (ser0 != null) {
        ValueBinder vb = new ValueBinder<>(SET_STRING, ser0);
        bind(stmt, param, vb, val);
        continue;
      }
      Function<Object, byte[]> ser1 = config.getBinarySerializer(mapType, key, valType);
      if (ser1 != null) {
        ValueBinder vb = new ValueBinder<>(SET_BYTES, ser1);
        bind(stmt, param, vb, val);
        continue;
      }
      ValueBinderFactory factory = ValueBinderFactory.getInstance();
      ValueBinder vb = factory.getDefaultBinder(valType);
      bind(stmt, param, vb, val);
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static void bind(PreparedStatement stmt,
        NamedParameter param,
        ValueBinder vb,
        Object val) throws Throwable {
    Object output = vb.getParamValue(val);
    if (LOG.isTraceEnabled()) {
      String key = param.name();
      if (vb.isAdaptive() && output != val) {
        LOG.trace("==> Parameter \"{}\": {} (original value: {})", key, output, val);
      } else {
        LOG.trace("==> Parameter \"{}\": {}", key, output);
      }
    }
    param.positions().forEachThrowing(i -> vb.bind(stmt, i, output));
  }

}
