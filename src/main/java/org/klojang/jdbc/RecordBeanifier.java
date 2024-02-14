package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.Err;
import org.klojang.jdbc.x.Utils;
import org.klojang.jdbc.x.rs.RecordFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.*;

import static org.klojang.check.CommonChecks.gt;
import static org.klojang.check.CommonChecks.yes;
import static org.klojang.check.CommonExceptions.STATE;
import static org.klojang.jdbc.x.Strings.LIMIT;

final class RecordBeanifier<T extends Record> implements BeanExtractor<T> {

  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(RecordBeanifier.class);

  private static class BeanIterator<T extends Record> implements Iterator<T> {

    private final RecordBeanifier<T> extractor;

    BeanIterator(RecordBeanifier<T> extractor) {
      this.extractor = extractor;
    }

    @Override
    public boolean hasNext() {
      return !extractor.empty;
    }

    @Override
    public T next() {
      Check.on(STATE, extractor.empty).is(yes(), Err.NO_MORE_ROWS);
      return extractor.extract().get();
    }
  }


  private final ResultSet rs;
  private final RecordFactory<T> factory;

  RecordBeanifier(ResultSet rs, RecordFactory<T> factory) {
    this.rs = rs;
    this.factory = factory;
  }

  private boolean empty;

  @Override
  public Optional<T> extract() {
    if (empty) {
      return Optional.empty();
    }
    try {
      Optional<T> bean = Optional.of(factory.createRecord(rs));
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
    int i = 0;
    try {
      do {
        beans.add(factory.createRecord(rs));
      } while (++i < limit && (empty = !rs.next()));
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
    return beans;
  }

  @Override
  public List<T> extractAll() {
    return extractAll(10);
  }

  @Override
  public List<T> extractAll(int sizeEstimate) {
    Check.that(sizeEstimate, "sizeEstimate").is(gt(), 0);
    if (empty) {
      return Collections.emptyList();
    }
    List<T> beans = new ArrayList<>(sizeEstimate);
    try {
      do {
        beans.add(factory.createRecord(rs));
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
