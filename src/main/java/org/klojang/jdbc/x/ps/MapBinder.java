package org.klojang.jdbc.x.ps;

import org.klojang.jdbc.BindInfo;
import org.klojang.jdbc.x.ps.writer.EnumWriterLookup;
import org.klojang.jdbc.x.sql.NamedParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
  public void bindMap(
      Map<String, Object> map,
      PreparedStatement ps,
      Collection<NamedParameter> bound)
      throws Throwable {
    ColumnWriterFinder writerFinder = ColumnWriterFinder.getInstance();
    for (NamedParameter param : params) {
      String key = param.name();
      if (!map.containsKey(key)) {
        continue;
      }
      bound.add(param);
      Object value = map.getOrDefault(key, ABSENT);
      if (value == null) {
        param.positions().forEachThrowing(i -> ps.setString(i, null));
      } else if (value != ABSENT) {
        ColumnWriter writer;
        if (value instanceof Enum && bindInfo.saveEnumAsString(Map.class, key)) {
          writer = EnumWriterLookup.ENUM_TO_STRING;
        } else {
          writer = writerFinder.getDefaultWriter(value.getClass());
        }
        Object output = writer.getParamValue(value);
        if (LOG.isDebugEnabled()) {
          if (value == output) {
            LOG.trace("-> Parameter \"{}\": {}", key, output);
          } else {
            LOG.trace("-> Parameter \"{}\": {} (map value: {})", key, output, value);
          }
        }
        param.positions().forEachThrowing(i -> writer.bind(ps, i, output));
      }
    }
  }
}
