package org.klojang.jdbc.x.ps;

import org.klojang.jdbc.BindInfo;
import org.klojang.jdbc.x.ps.writer.EnumBinderLookup;
import org.klojang.jdbc.x.sql.NamedParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MapBinder {

  private static final Logger LOG = LoggerFactory.getLogger(MapBinder.class);

  private static final Object ABSENT = new Object();

  private final List<NamedParameter> params;
  private final BindInfo bindInfo;

  public MapBinder(List<NamedParameter> params, BindInfo bindInfo) {
    this.params = params;
    this.bindInfo = bindInfo;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void bind(Map<String, Object> map,
        PreparedStatement ps,
        Set<NamedParameter> bound)
        throws Throwable {
    ColumnWriterFactory factory = ColumnWriterFactory.getInstance();
    for (NamedParameter param : params) {
      String key = param.name();
      if (!map.containsKey(key)) {
        continue;
      }
      bound.add(param);
      Object value = map.getOrDefault(key, ABSENT);
      if (value == null) {
        // Don't try to refactor away this case. Not worth it.
        param.positions().forEachThrowing(i -> ps.setString(i, null));
      } else if (value != ABSENT) {
        ValueBinder binder;
        if (value instanceof Enum && bindInfo.saveEnumAsString(Map.class, key)) {
          binder = EnumBinderLookup.ENUM_TO_STRING;
        } else {
          binder = factory.getDefaultWriter(value.getClass());
        }
        Object output = binder.getParamValue(value);
        if (LOG.isTraceEnabled()) {
          if (binder.isAdaptive() && output != value) {
            LOG.trace("==> Parameter \"{}\": {} (map value: {})", key, output, value);
          } else {
            LOG.trace("==> Parameter \"{}\": {}", key, output);
          }
        }
        param.positions().forEachThrowing(i -> binder.bind(ps, i, output));
      }
    }
  }

  ValueBinder findBinder(String key, Object val) {
    if (val instanceof Enum && bindInfo.saveEnumAsString(Map.class, key)) {
      return EnumBinderLookup.ENUM_TO_STRING;
    } else {
      return ColumnWriterFactory.getInstance().getDefaultWriter(val.getClass());
    }
  }
}
