package com.mirantis.cachemod.filter;

import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

public abstract class AbstractKeyProvider implements KeyProvider {

  protected abstract String shortQueryString(String queryString);
  
  @Override
  public String createKey(HttpServletRequest request) {
    StringBuilder cBuffer = new StringBuilder(20);

    String generatedKey = request.getRequestURI();

    if (generatedKey.charAt(0) != '/') {
      cBuffer.append('/');
    }

    cBuffer.append(generatedKey);
    cBuffer.append("_").append(request.getMethod()).append("_");

    String queryString = getQueryString(request);

    if (queryString != null) {
      cBuffer.append('_');
      cBuffer.append(shortQueryString(queryString));
    }

    return cBuffer.toString();
  }

  protected String getQueryString(HttpServletRequest request) {
    Map paramMap = request.getParameterMap();
    if (paramMap.isEmpty()) {
      return "";
    }

    StringBuilder buf = new StringBuilder();
    boolean first = true;
    for (Map.Entry<String, String[]> entry : new TreeMap<String, String[]>(paramMap).entrySet()) {
      if (!"jsessionid".equals(entry.getKey())) {
        for (String value : entry.getValue()) {
          if (first) {
            first = false;
          } else {
            buf.append('&');
          }
          buf.append(entry.getKey()).append('=').append(value);
        }
      }
    }

    if (buf.length() == 0) {
      return "";
    } else {
      return buf.toString();
    }
  }
  
}
