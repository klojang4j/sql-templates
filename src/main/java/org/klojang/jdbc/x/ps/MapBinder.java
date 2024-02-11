package org.klojang.jdbc.x.ps;

import org.klojang.jdbc.BindInfo;
import org.klojang.jdbc.x.ps.writer.StringBinderLookup;
import org.klojang.jdbc.x.sql.NamedParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.klojang.jdbc.BindInfo.ManualBinder;

/**
 * Binds the values within in a Map to a PreparedStatement.
 */
public final class MapBinder {

  private static final Logger LOG = LoggerFactory.getLogger(MapBinder.class);

  private final List<NamedParameter> params;
  private final BindInfo bindInfo;

  public MapBinder(List<NamedParameter> params, BindInfo bindInfo) {
    this.params = params;
    this.bindInfo = bindInfo;
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
      final ManualBinder manual;
      if (val != null) {
        manual = bindInfo.getManualBinder(val.getClass(), map.getClass(), key);
      } else {
        manual = null;
      }
      if (manual != null) {
        if (LOG.isTraceEnabled()) {
          LOG.trace("==> Parameter \"{}\": {} (manually bound)", key, val);
        }
        param.positions().forEachThrowing(i -> manual.bind(ps, i, val));
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

  @SuppressWarnings("rawtypes")
  ValueBinder findBinder(Map<String, Object> map, String key, Object val) {
    if (val == null) {
      // Return any ValueBinder. For null values *all* binders will just call
      // PreparedStatement.setString(paramIndex, null);
      return StringBinderLookup.DEFAULT;
    } else if (val instanceof Enum && bindInfo.saveEnumAsString(map.getClass(), key)) {
      return ValueBinder.ANY_TO_STRING;
    } else {
      Integer sqlType = bindInfo.getSqlType(val.getClass(), map.getClass(), key);
      if (sqlType == null) {
        return ValueBinderFactory.getInstance().getDefaultBinder(val.getClass());
      }
      return ValueBinderFactory.getInstance().getBinder(val.getClass(), sqlType);
    }
  }
}
