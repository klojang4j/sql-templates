package org.klojang.jdbc;

import java.util.*;

final class EmptyMappifier implements ResultSetMappifier {

  static final EmptyMappifier INSTANCE = new EmptyMappifier();

  @Override
  public Optional<Map<String,Object>> mappify() {
    return Optional.empty();
  }

  @Override
  public List<Map<String,Object>> mappify(int limit) {
    return Collections.emptyList();
  }

  @Override
  public List<Map<String,Object>> mappifyAll() {
    return Collections.emptyList();
  }

  @Override
  public List<Map<String,Object>> mappifyAll(int sizeEstimate) {
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
