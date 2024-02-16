package org.klojang.jdbc;

import org.junit.jupiter.api.Test;
import org.klojang.jdbc.x.Utils;
import org.klojang.templates.NameMapper;

import java.time.DayOfWeek;

import static org.junit.jupiter.api.Assertions.*;
import static org.klojang.templates.name.SnakeCaseToCamelCase.snakeCaseToCamelCase;
import static org.klojang.templates.name.WordCaseToCamelCase.wordCaseToCameCase;

public class SessionConfigTest {

  @Test
  public void withPropertyToColumnMapper00() {
    NameMapper mapper = wordCaseToCameCase();
    SessionConfig config = SessionConfig.getDefaultConfig()
          .withPropertyToColumnMapper(mapper);
    assertSame(mapper, config.getPropertyToColumnMapper());
    assertNull(config.getCustomBinder(null, null, null));
    assertNull(config.getCustomReader(null, null, null, 0));
    assertNull(config.getSqlType(null, null, null));
    assertSame(snakeCaseToCamelCase().getClass(),
          config.getColumnToPropertyMapper().getClass());
  }

  @Test
  public void withSaveAllEnumsAsStrings00() {
    SessionConfig config = SessionConfig.getDefaultConfig().withEnumsSavedAsStrings();
    assertTrue(config.saveEnumAsString(null, null, DayOfWeek.class));
  }
}
