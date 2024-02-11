package org.klojang.jdbc.util;

import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.*;
import static org.klojang.util.ArrayMethods.implode;
import static org.klojang.util.ArrayMethods.implodeInts;

public class SQLTypeUtilTest {

  @Test
  public void getTypeName00() {
    assertEquals("VARCHAR", SQLTypeUtil.getTypeName(Types.VARCHAR));
    assertEquals("INTEGER", SQLTypeUtil.getTypeName(Types.INTEGER));
    assertEquals("TIMESTAMP", SQLTypeUtil.getTypeName(Types.TIMESTAMP));
    assertEquals("TINYINT", SQLTypeUtil.getTypeName(Types.TINYINT));
  }

  @Test()
  public void getTypeName01() {
    assertThrows(IllegalArgumentException.class, () -> SQLTypeUtil.getTypeName(-17));
  }

  @Test()
  public void forName00() {
    assertEquals(Types.BOOLEAN, SQLTypeUtil.forName("BOOLEAN"));
  }

  @Test()
  public void forName01() {
    assertThrows(IllegalArgumentException.class, () -> SQLTypeUtil.forName("FOO BAR"));
  }

  @Test()
  public void isValidType00() {
    assertTrue(SQLTypeUtil.isValidValue(Types.BIGINT));
    assertFalse(SQLTypeUtil.isValidValue(-17));
  }

  @Test()
  public void isValidName00() {
    assertTrue(SQLTypeUtil.isValidName("BIGINT"));
    assertFalse(SQLTypeUtil.isValidName("FOO BAR"));
  }

  @Test
  public void getAllTypeNames00() {
    // Just call to make sure we can
    System.out.println(implode(SQLTypeUtil.getAllNames()));
  }

  @Test
  public void getAllTypes00() {
    // Just call to make sure we can
    System.out.println(implodeInts(SQLTypeUtil.getAllValues()));
  }

  @Test
  public void printAll00() {
    // Just call to make sure we can
    SQLTypeUtil.printAllValues(System.out);
  }

  @Test
  public void printAllNames00() {
    // Just call to make sure we can
    SQLTypeUtil.printAllNames(System.out);
  }
}
