package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.Utils;
import org.klojang.jdbc.x.rs.PropertyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.*;
import java.util.function.Supplier;

import static org.klojang.check.CommonChecks.gt;
import static org.klojang.check.CommonChecks.yes;
import static org.klojang.check.CommonExceptions.STATE;
import static org.klojang.jdbc.x.Strings.*;

final class DefaultBeanifier<T> implements ResultSetBeanifier<T> {

  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(DefaultBeanifier.class);

  private static class BeanIterator<T> implements Iterator<T> {

    private final DefaultBeanifier<T> beanifier;

    BeanIterator(DefaultBeanifier<T> beanifier) {
      this.beanifier = beanifier;
    }

    @Override
    public boolean hasNext() {
      return !beanifier.empty;
    }

    @Override
    public T next() {
      Check.on(STATE, beanifier.empty).is(yes(), NO_MORE_ROWS);
      return beanifier.beanify().get();
    }
  }

  private final ResultSet rs;
  private final PropertyWriter<?, ?>[] writers;
  private final Supplier<T> beanSupplier;

  private boolean empty;

  DefaultBeanifier(ResultSet rs, PropertyWriter<?, ?>[] writers, Supplier<T> supplier) {
    this.rs = rs;
    this.writers = writers;
    this.beanSupplier = supplier;
  }

  @Override
  public Optional<T> beanify() {
    if (empty) {
      return Optional.empty();
    }
    try {
      Optional<T> bean = Optional.of(PropertyWriter.writeAll(rs, beanSupplier, writers));
      empty = !rs.next();
      return bean;
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  @Override
  public List<T> beanify(int limit) {
    Check.that(limit, LIMIT).is(gt(), 0);
    if (empty) {
      return Collections.emptyList();
    }
    List<T> beans = new ArrayList<>(limit);
    int i = 0;
    try {
      do {
        beans.add(PropertyWriter.writeAll(rs, beanSupplier, writers));
      } while (++i < limit && (empty = !rs.next()));
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
    return beans;
  }

  @Override
  public List<T> beanifyAll() {
    return beanifyAll(10);
  }

  @Override
  public List<T> beanifyAll(int sizeEstimate) {
    Check.that(sizeEstimate, SIZE_ESTIMATE).is(gt(), 0);
    if (empty) {
      return Collections.emptyList();
    }
    List<T> beans = new ArrayList<>(sizeEstimate);
    try {
      do {
        beans.add(PropertyWriter.writeAll(rs, beanSupplier, writers));
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
