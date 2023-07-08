package org.klojang.jdbc.x.sql;

import org.klojang.invoke.Getter;
import org.klojang.templates.NameMapper;

import java.sql.Connection;
import java.util.Map;
import java.util.function.BiFunction;

public record BatchInsertConfig<T>(
    Connection connection,
    Class<T> beanClass,
    String tableName,
    int chunkSize,
    boolean commitPerChunk,
    Map<String, Getter> getters,
    Map<String, BiFunction<T, Object, Object>> transformers,
    NameMapper mapper
) {

}
