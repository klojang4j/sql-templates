package org.klojang.jdbc.x.ps;

import org.klojang.jdbc.SessionConfig;
import org.klojang.jdbc.x.ps.writer.EnumBinderLookup;
import org.klojang.jdbc.x.ps.writer.StringBinderLookup;
import org.klojang.jdbc.x.sql.NamedParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.klojang.jdbc.SessionConfig.CustomBinder;

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
  public void bind(Map<String, Object> map,
        PreparedStatement ps,
        Set<NamedParameter> bound) throws Throwable {
    for (NamedParameter param : params) {
      String key = param.name();
      if (!map.containsKey(key)) {
        continue;
      }
      bound.add(param);
      Object val = map.get(key);
      CustomBinder cb = val == null
            ? null
            : config.getCustomBinder(val.getClass(), map.getClass(), key);
      if (cb != null) {
        if (LOG.isTraceEnabled()) {
          LOG.trace("==> Parameter \"{}\": {} (using custom binder)", key, val);
        }
        param.positions().forEachThrowing(i -> cb.bind(ps, i, val));
      } else {
        ValueBinder vb = findBinder(map, key, val);
        Object output = vb.getParamValue(val);
        if (LOG.isTraceEnabled()) {
          if (vb.isAdaptive() && output != val) {
            LOG.trace("==> Parameter \"{}\": {} (original value: {})", key, output, val);
          } else {
            LOG.trace("==> Parameter \"{}\": {}", key, output);
          }
        }
        param.positions().forEachThrowing(i -> vb.bind(ps, i, output));
      }
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  ValueBinder findBinder(Map<String, Object> map, String key, Object val) {
    if (val == null) {
      // Return any ValueBinder. For null values *all* binders will just call
      // PreparedStatement.setString(paramIndex, null);
      return StringBinderLookup.DEFAULT;
    } else {
      Integer sqlType = config.getSqlType(map.getClass(), key, val.getClass());
      if (sqlType == null) {
        if (val instanceof Enum) {
          Class<? extends Enum<?>> type = (Class<? extends Enum<?>>) val.getClass();
          if (config.saveEnumAsString(map.getClass(), key, type)) {
            return ValueBinder.ANY_TO_STRING;
          }
          return EnumBinderLookup.DEFAULT;
        }
        return ValueBinderFactory.getInstance().getDefaultBinder(val.getClass());
      }
      return ValueBinderFactory.getInstance().getBinder(val.getClass(), sqlType);
    }
  }
}
