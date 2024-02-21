package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.Err;
import org.klojang.jdbc.x.Utils;
import org.klojang.jdbc.x.rs.PropertyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.*;
import java.util.function.Supplier;

import static org.klojang.check.CommonChecks.gt;
import static org.klojang.check.CommonChecks.no;
import static org.klojang.check.CommonExceptions.STATE;
import static org.klojang.jdbc.x.Strings.LIMIT;
import static org.klojang.jdbc.x.Strings.SIZE_ESTIMATE;

final class DefaultBeanExtractor<T> implements BeanExtractor<T> {

  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(DefaultBeanExtractor.class);

  private static class BeanIterator<T> implements Iterator<T> {

    private final DefaultBeanExtractor<T> extractor;

    BeanIterator(DefaultBeanExtractor<T> extractor) {
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
  private final PropertyWriter<?, ?>[] writers;
  private final Supplier<T> beanSupplier;

  private boolean empty;

  DefaultBeanExtractor(ResultSet rs,
        PropertyWriter<?, ?>[] writers,
        Supplier<T> supplier) {
    this.rs = rs;
    this.writers = writers;
    this.beanSupplier = supplier;
  }

  @Override
  public Optional<T> extract() {
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
  public List<T> extract(int limit) {
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
  public List<T> extractAll() {
    return extractAll(10);
  }

  @Override
  public List<T> extractAll(int sizeEstimate) {
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
