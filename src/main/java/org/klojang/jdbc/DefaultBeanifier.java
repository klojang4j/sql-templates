package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.rs.BeanChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.*;
import java.util.function.Supplier;

import static org.klojang.check.CommonChecks.gt;
import static org.klojang.check.CommonChecks.yes;
import static org.klojang.check.CommonExceptions.STATE;
import static org.klojang.jdbc.x.rs.BeanChannel.toBean;

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
      return beanifier.empty;
    }

    @Override
    public T next() {
      Check.on(STATE, beanifier.empty).is(yes(), "no more rows in result set");
      return beanifier.beanify().get();
    }
  }

  private final ResultSet rs;
  private final BeanChannel<?, ?>[] channels;
  private final Supplier<T> beanSupplier;

  private boolean empty;

  DefaultBeanifier(ResultSet rs, BeanChannel<?, ?>[] channels, Supplier<T> supplier) {
    this.rs = rs;
    this.channels = channels;
    this.beanSupplier = supplier;
  }

  @Override
  public Optional<T> beanify() {
    if (empty) {
      return Optional.empty();
    }
    try {
      Optional<T> bean = Optional.of(toBean(rs, beanSupplier, channels));
      empty = !rs.next();
      return bean;
    } catch (Throwable t) {
      throw KlojangSQLException.wrap(t, null);
    }
  }

  @Override
  public List<T> beanify(int limit) {
    Check.that(limit, "limit").is(gt(), 0);
    if (empty) {
      return Collections.emptyList();
    }
    List<T> beans = new ArrayList<>(limit);
    int i = 0;
    try {
      do {
        beans.add(toBean(rs, beanSupplier, channels));
      } while (++i < limit && (empty = !rs.next()));
    } catch (Throwable t) {
      throw KlojangSQLException.wrap(t, null);
    }
    return beans;
  }

  @Override
  public List<T> beanifyAll() {
    return beanifyAll(10);
  }

  @Override
  public List<T> beanifyAll(int sizeEstimate) {
    Check.that(sizeEstimate, "sizeEstimate").is(gt(), 0);
    if (empty) {
      return Collections.emptyList();
    }
    List<T> beans = new ArrayList<>(sizeEstimate);
    try {
      do {
        beans.add(toBean(rs, beanSupplier, channels));
      } while (rs.next());
    } catch (Throwable t) {
      throw KlojangSQLException.wrap(t, null);
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
