/*
 * Copyright 2011 Mirantis Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
