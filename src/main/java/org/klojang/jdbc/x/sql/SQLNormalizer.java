package org.klojang.jdbc.x.sql;

import org.klojang.check.Check;
import org.klojang.jdbc.KJSQLException;
import org.klojang.util.CollectionMethods;
import org.klojang.util.MutableInt;
import org.klojang.util.collection.IntArrayList;
import org.klojang.util.collection.IntList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.klojang.check.Check.fail;
import static org.klojang.check.CommonChecks.blank;
import static org.klojang.check.CommonChecks.ne;
import static org.klojang.util.CollectionMethods.collectionToList;

/*
 * Extracts named parameters from a SQL query string and replaces them with positional
 * parameters (question marks).
 *
 */
public final class SQLNormalizer {

  private static final String ERR_ADJACENT_PARAMS =
        "adjacent parameters at positions ${0} and ${1} cannot yield valid SQL";
  private static final String ERR_EMPTY_NAME =
        "zero-length parameter name at position ${0}";

  private final String normalized;
  private final Map<String, IntList> positions;
  private final List<NamedParameter> params;

  SQLNormalizer(String sql) {
    Check.that(sql).isNot(blank());
    Map<String, IntList> tmp = new LinkedHashMap<>();
    StringBuilder out = new StringBuilder(sql.length());
    MutableInt pCount = new MutableInt(); // parameter counter
    int pStartPos = -1;
    boolean inString = false;
    boolean escaped = false;
    StringBuilder param = null;
    for (int i = 0; i < sql.length(); ++i) {
      char c = sql.charAt(i);
      if (inString) {
        out.append(c);
        if (c == '\'') {
          if (!escaped) {
            inString = false;
          } else {
            escaped = true;
          }
        } else if (c == '\\') {
          escaped = true;
        } else {
          escaped = false;
        }
      } else if (pStartPos != -1) { // we are assembling a parameter name
        if (isParamChar(c)) {
          param.append(c);
          if (i == sql.length() - 1) {
            addParam(tmp, param, pCount, pStartPos);
          }
        } else {
          addParam(tmp, param, pCount, pStartPos);
          out.append(c);
          pStartPos = -1;
          if (c == '\'') {
            inString = true;
          } else if (c == ':') {
            fail(KJSQLException::new, ERR_ADJACENT_PARAMS, pStartPos, i);
          }
        }
      } else if (c == ':') {
        out.append('?');
        pStartPos = i;
        param = new StringBuilder();
      } else {
        out.append(c);
        if (c == '\'') {
          inString = true;
        }
      }
    }
    this.normalized = out.toString();
    this.positions = CollectionMethods.freeze(tmp, IntList::copyOf);
    this.params = collectionToList(tmp.entrySet(), this::toNamedParam);
  }


  public String getNormalizedSQL() {
    return normalized;
  }

  public List<NamedParameter> getParameters() {
    return params;
  }

  public Map<String, IntList> getParameterPositions() {
    return positions;
  }

  private static boolean isParamChar(char c) {
    return Character.isLetterOrDigit(c) || c == '_';
  }

  private static void addParam(
        Map<String, IntList> paramMap,
        StringBuilder param,
        MutableInt pCount,
        int startPos) {
    Check.on(KJSQLException::new, param)
          .has(StringBuilder::length, ne(), 0, ERR_EMPTY_NAME, startPos);
    paramMap.computeIfAbsent(param.toString(), k -> new IntArrayList())
          .add(pCount.ppi());
  }

  private NamedParameter toNamedParam(Entry<String, IntList> e) {
    return new NamedParameter(e.getKey(), IntList.copyOf(e.getValue()));
  }

}
