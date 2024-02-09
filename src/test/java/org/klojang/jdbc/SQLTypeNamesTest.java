package org.klojang.jdbc;

import java.sql.Types;
import org.junit.jupiter.api.Test;
import org.klojang.jdbc.util.SQLTypeUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SQLTypeNamesTest {

  @Test
  public void test00() {
    assertEquals("VARCHAR", SQLTypeUtil.getTypeName(Types.VARCHAR));
    assertEquals("INTEGER", SQLTypeUtil.getTypeName(Types.INTEGER));
    assertEquals("TIMESTAMP", SQLTypeUtil.getTypeName(Types.TIMESTAMP));
    assertEquals("TINYINT", SQLTypeUtil.getTypeName(Types.TINYINT));
  }

  @Test()
  public void test01() {
    assertThrows(IllegalArgumentException.class, () -> SQLTypeUtil.getTypeName(-17));
  }

  @Test
  public void test02() {
    // Just call to make sure we can
    SQLTypeUtil.printAll(System.out);
  }
}
