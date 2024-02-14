package org.klojang.jdbc.x.rs;

import org.klojang.check.Check;
import org.klojang.jdbc.MapExtractor;
import org.klojang.jdbc.x.Err;
import org.klojang.jdbc.x.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.*;

import static org.klojang.check.CommonChecks.gt;
import static org.klojang.check.CommonChecks.no;
import static org.klojang.check.CommonExceptions.STATE;
import static org.klojang.jdbc.x.Strings.LIMIT;
import static org.klojang.jdbc.x.rs.KeyWriter.toMap;

public final class DefaultMapExtractor implements MapExtractor {

  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(DefaultMapExtractor.class);

  private static class MapIterator implements Iterator<Map<String, Object>> {

    private final DefaultMapExtractor m;

    MapIterator(DefaultMapExtractor m) {
      this.m = m;
    }

    @Override
    public boolean hasNext() {
      return !m.isEmpty();
    }

    @Override
    public Map<String, Object> next() {
      Check.on(STATE, m.isEmpty()).is(no(), Err.NO_MORE_ROWS);
      return m.extract().get();
    }
  }

  private final ResultSet rs;
  private final KeyWriter<?>[] writers;

  private boolean empty;

  DefaultMapExtractor(ResultSet rs, KeyWriter<?>[] writers) {
    this.rs = rs;
    this.writers = writers;
  }

  @Override
  public Optional<Map<String, Object>> extract() {
    if (empty) {
      return Optional.empty();
    }
    try {
      Optional<Map<String, Object>> row = Optional.of(toMap(rs, writers));
      empty = !rs.next();
      return row;
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  @Override
  public List<Map<String, Object>> extract(int limit) {
    Check.that(limit, LIMIT).is(gt(), 0);
    if (empty) {
      return Collections.emptyList();
    }
    List<Map<String, Object>> all = new ArrayList<>(limit);
    int i = 0;
    try {
      do {
        all.add(toMap(rs, writers));
      } while (++i < limit && (empty = !rs.next()));
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
    return all;
  }

  @Override
  public List<Map<String, Object>> extractAll() {
    return extractAll(10);
  }

  @Override
  public List<Map<String, Object>> extractAll(int sizeEstimate) {
    Check.that(sizeEstimate, "sizeEstimate").is(gt(), 0);
    if (empty) {
      return Collections.emptyList();
    }
    List<Map<String, Object>> all = new ArrayList<>(sizeEstimate);
    try {
      do {
        all.add(toMap(rs, writers));
      } while (rs.next());
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
    empty = true;
    return all;
  }

  @Override
  public boolean isEmpty() {
    return empty;
  }

  @Override
  public Iterator<Map<String, Object>> iterator() {
    return new MapIterator(this);
  }
}