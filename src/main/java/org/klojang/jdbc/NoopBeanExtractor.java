package org.klojang.jdbc;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

final class NoopBeanExtractor<T> implements BeanExtractor<T> {

  @SuppressWarnings("rawtypes")
  static final NoopBeanExtractor INSTANCE = new NoopBeanExtractor();

  private NoopBeanExtractor() {}

  @Override
  public Optional<T> extract() {
    return Optional.empty();
  }

  @Override
  public List<T> extract(int limit) {
    return Collections.emptyList();
  }

  @Override
  public List<T> extractAll() {
    return Collections.emptyList();
  }

  @Override
  public List<T> extractAll(int sizeEstimate) {
    return Collections.emptyList();
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public Iterator<T> iterator() {
    return Collections.emptyIterator();
  }
}
