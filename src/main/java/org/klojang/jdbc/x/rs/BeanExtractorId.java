package org.klojang.jdbc.x.rs;

import org.klojang.jdbc.SessionConfig;

record BeanExtractorId(Class<?> clazz, SessionConfig config, ResultSetId resultSetId) { }
