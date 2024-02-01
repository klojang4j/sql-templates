package org.klojang.jdbc.x.sql;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.klojang.util.collection.IntList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParamExtractorTest {

  @Test
  public void test00() {
    String s = "SELECT FOO FROM BAR WHERE FULL_NAME = :fullName";
    ParamExtractor extractor = new ParamExtractor(s);
    List<NamedParameter> params = extractor.getParameters();
    assertEquals(1, params.size());
    assertEquals("fullName", params.get(0).name());
    assertEquals(1, params.get(0).positions().size());
    assertEquals(1, params.get(0).positions().get(0));
    assertEquals(
        "SELECT FOO FROM BAR WHERE FULL_NAME = ?",
        extractor.getNormalizedSQL());
  }

  @Test
  public void test01() {
    String s = """
        SELECT FOO
          FROM BAR 
         WHERE FULL_NAME = :name
           AND LAST_NAME = :lastName 
            OR LAST_NAME = :name
         LIMIT :from,:to
        """;
    ParamExtractor extractor = new ParamExtractor(s);
    Map<String, IntList> paramMap = extractor.getParameterPositions();
    assertEquals(4, paramMap.size());
    assertEquals(IntList.of(1, 3), paramMap.get("name"));
    assertEquals(IntList.of(2), paramMap.get("lastName"));
    assertEquals(IntList.of(4), paramMap.get("from"));
    assertEquals(IntList.of(5), paramMap.get("to"));
    List<NamedParameter> params = extractor.getParameters();
    assertEquals(4, params.size());
    assertEquals("name", params.get(0).name());
    assertEquals("lastName", params.get(1).name());
    assertEquals("from", params.get(2).name());
    assertEquals("to", params.get(3).name());
    assertEquals(IntList.of(1, 3), params.get(0).positions());
  }

  @Test
  public void test02() {
    String s = "SELECT * FROM FROM WHERE NAME = 'BAR'";
    ParamExtractor extractor = new ParamExtractor(s);
    Map<String, IntList> paramMap = extractor.getParameterPositions();
    assertEquals(0, paramMap.size());
    assertEquals(s, extractor.getNormalizedSQL());
  }

  @Test
  public void test03() {
    String s = "SELECT * FROM FROM WHERE NAME = ':BAR'";
    ParamExtractor extractor = new ParamExtractor(s);
    Map<String, IntList> paramMap = extractor.getParameterPositions();
    assertEquals(0, paramMap.size());
    assertEquals(s, extractor.getNormalizedSQL());
  }

  @Test
  public void test04() {
    String s = "SELECT * FROM FROM WHERE NAME = ':::BAR'";
    ParamExtractor extractor = new ParamExtractor(s);
    Map<String, IntList> paramMap = extractor.getParameterPositions();
    assertEquals(0, paramMap.size());
    assertEquals(s, extractor.getNormalizedSQL());
  }

  @Test
  public void test05() {
    String s = "SELECT * FROM FROM WHERE NAME = '\\'BAR'";
    ParamExtractor extractor = new ParamExtractor(s);
    Map<String, IntList> paramMap = extractor.getParameterPositions();
    assertEquals(0, paramMap.size());
    assertEquals(s, extractor.getNormalizedSQL());
  }

  @Test
  public void test06() {
    String s = "SELECT * FROM FROM WHERE NAME = 'B\\'AR'";
    ParamExtractor extractor = new ParamExtractor(s);
    Map<String, IntList> paramMap = extractor.getParameterPositions();
    assertEquals(0, paramMap.size());
    assertEquals(s, extractor.getNormalizedSQL());
  }

  @Test
  public void test07() {
    String s = "SELECT * FROM FROM WHERE NAME = 'BAR\\''";
    ParamExtractor extractor = new ParamExtractor(s);
    Map<String, IntList> paramMap = extractor.getParameterPositions();
    assertEquals(0, paramMap.size());
    assertEquals(s, extractor.getNormalizedSQL());
  }

}

