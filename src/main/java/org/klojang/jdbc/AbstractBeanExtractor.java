package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.check.fallible.FallibleFunction;
import org.klojang.jdbc.x.Err;
import org.klojang.jdbc.x.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.*;

import static org.klojang.check.CommonChecks.gt;
import static org.klojang.check.CommonChecks.present;
import static org.klojang.check.CommonExceptions.STATE;
import static org.klojang.jdbc.x.Strings.LIMIT;
import static org.klojang.jdbc.x.Strings.SIZE_ESTIMATE;

abstract sealed class AbstractBeanExtractor<T> implements BeanExtractor<T> permits
      DefaultBeanExtractor,
      DefaultMapExtractor,
      RecordExtractor,
      CustomExtractor {

  private static class BeanIterator<T> implements Iterator<T> {

    private final AbstractBeanExtractor<T> extractor;

    private Optional<T> prefetched;

    BeanIterator(AbstractBeanExtractor<T> extractor) {
      this.extractor = extractor;
      this.prefetched = extractor.extract();
    }

    @Override
    public boolean hasNext() {
      return prefetched.isPresent();
    }

    @Override
    public T next() {
      Check.on(STATE, prefetched).is(present(), Err.NO_MORE_ROWS);
      T bean = prefetched.get();
      prefetched = extractor.extract();
      return bean;
    }
  }

  @SuppressWarnings({"unused"})
  private static final Logger LOG = LoggerFactory.getLogger(SQLInsert.class);

  private final ResultSet rs;
  private final FallibleFunction<ResultSet, T, ? extends Throwable> converter;

  private T first;
  private boolean empty;

  AbstractBeanExtractor(ResultSet rs,
        FallibleFunction<ResultSet, T, ? extends Throwable> converter) {
    this.rs = rs;
    this.converter = converter;
    try {
      if (rs.next()) {
        this.first = converter.apply(rs);
      } else {
        this.empty = true;
      }
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  @Override
  public Optional<T> extract() {
    if (empty) {
      return Optional.empty();
    }
    var first = this.first;
    if (first != null) {
      this.first = null;
      return Optional.of(first);
    }
    try {
      if (rs.next()) {
        return Optional.of(converter.apply(rs));
      }
      empty = true;
      return Optional.empty();
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  @Override
  public List<T> extract(int limit) {
    Check.that(limit, LIMIT).is(gt(), 0);
    if (empty) {
      return Collections.emptyList();
    }
    List<T> all = new ArrayList<>(limit);
    var first = this.first;
    if (first != null) {
      this.first = null;
      all.add(first);
    }
    try {
      for (int i = (first == null ? 0 : 1); i < limit; ++i) {
        if (!rs.next()) {
          empty = true;
          break;
        }
        all.add(converter.apply(rs));
      }
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
    return all;
  }

  @Override
  public List<T> extractAll(int sizeEstimate) {
    Check.that(sizeEstimate, SIZE_ESTIMATE).is(gt(), 0);
    if (empty) {
      return Collections.emptyList();
    }
    List<T> all = new ArrayList<>(sizeEstimate);
    var first = this.first;
    if (first != null) {
      this.first = null;
      all.add(first);
    }
    try {
      while (rs.next()) {
        all.add(converter.apply(rs));
      }
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
  public Iterator<T> iterator() {
    return new BeanIterator<>(this);
  }

}
