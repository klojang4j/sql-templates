package org.klojang.jdbc.x.sql;

import org.klojang.invoke.Getter;
import org.klojang.jdbc.Transformer;
import org.klojang.templates.NameMapper;

import java.sql.Connection;

public record BatchInsertConfig<T>(
    Connection connection,
    Class<T> beanClass,
    String tableName,
    int chunkSize,
    boolean commitPerChunk,
    Getter[] getters,
    Transformer[] transformers,
    NameMapper mapper
) {

}
