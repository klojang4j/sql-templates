package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.check.fallible.FallibleFunction;
import org.klojang.jdbc.x.Err;
import org.klojang.jdbc.x.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.klojang.check.CommonChecks.gt;
import static org.klojang.check.CommonChecks.no;
import static org.klojang.check.CommonExceptions.STATE;
import static org.klojang.jdbc.x.Strings.LIMIT;
import static org.klojang.jdbc.x.Strings.SIZE_ESTIMATE;

final class CustomExtractor<T> implements BeanExtractor<T> {

  private static final Logger LOG = LoggerFactory.getLogger(CustomExtractor.class);

  private static class BeanIterator<T> implements Iterator<T> {

    private final CustomExtractor<T> extractor;

    BeanIterator(CustomExtractor<T> extractor) {
      this.extractor = extractor;
    }

    @Override
    public boolean hasNext() {
      return !extractor.empty;
    }

    @Override
    public T next() {
      Check.on(STATE, extractor.empty).is(no(), Err.NO_MORE_ROWS);
      return extractor.extract().get();
    }
  }


  private final ResultSet rs;
  private final FallibleFunction<ResultSet, T, SQLException> converter;

  private boolean empty;

  CustomExtractor(ResultSet rs, FallibleFunction<ResultSet, T, SQLException> converter) {
    this.rs = rs;
    this.converter = converter;
  }

  @Override
  public Optional<T> extract() {
    if (empty) {
      return Optional.empty();
    }
    try {
      Optional<T> bean = Optional.of(converter.apply(rs));
      empty = !rs.next();
      return bean;
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
    List<T> beans = new ArrayList<>(limit);
    try {
      for (int i = 0; i < limit; ++i) {
        beans.add(converter.apply(rs));
        if (!rs.next()) {
          empty = true;
          break;
        }
      }
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
    return beans;
  }

  @Override
  public List<T> extractAll(int sizeEstimate) {
    Check.that(sizeEstimate, SIZE_ESTIMATE).is(gt(), 0);
    if (empty) {
      return Collections.emptyList();
    }
    List<T> beans = new ArrayList<>(sizeEstimate);
    ResultSet rs = this.rs;
    try {
      do {
        beans.add(converter.apply(rs));
      } while (rs.next());
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
    empty = true;
    return beans;
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
