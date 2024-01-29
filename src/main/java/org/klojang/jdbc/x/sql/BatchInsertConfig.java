package org.klojang.jdbc.x.sql;

import org.klojang.invoke.BeanReader;
import org.klojang.jdbc.BeanValueProcessor;
import org.klojang.templates.NameMapper;

import java.sql.Connection;

public record BatchInsertConfig<T>(
      Connection connection,
      BeanReader<T> reader,
      BeanValueProcessor<T> processor,
      NameMapper mapper,
      String tableName,
      int chunkSize,
      boolean commitPerChunk
) { }
