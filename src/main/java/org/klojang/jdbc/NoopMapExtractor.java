package org.klojang.jdbc;

import java.util.*;

final class NoopMapExtractor implements MapExtractor {

  static final NoopMapExtractor INSTANCE = new NoopMapExtractor();

  @Override
  public Optional<Map<String,Object>> extract() {
    return Optional.empty();
  }

  @Override
  public List<Map<String,Object>> extract(int limit) {
    return Collections.emptyList();
  }

  @Override
  public List<Map<String,Object>> extractAll() {
    return Collections.emptyList();
  }

  @Override
  public List<Map<String,Object>> extractAll(int sizeEstimate) {
    return Collections.emptyList();
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public Iterator<Map<String,Object>> iterator() {
    return Collections.emptyIterator();
  }
}
