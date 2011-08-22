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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.mirantis.cachemod.filter.CacheConfiguration.ExpiresType;
import com.mirantis.cachemod.filter.CacheConfiguration.LastModifiedType;

public class CacheHttpServletResponse extends HttpServletResponseWrapper {

  private CacheServletOutputStream out = null;
  private PrintWriter writer = null;
  private int status = SC_OK;

  private CacheEntry cacheEntry;
  private CacheConfiguration conf;
  private boolean fragment;

  public CacheHttpServletResponse(HttpServletResponse response, CacheEntry cacheEntry, CacheConfiguration conf, boolean fragment) {
    super(response);
    this.cacheEntry = cacheEntry;
    this.conf = conf;
    this.fragment = fragment;

    if (!fragment) {

      long current = currentTime();

      if (conf.getLastModified().equals(LastModifiedType.INITIAL)) {
        cacheEntry.setLastModified(current);
        super.setDateHeader("Last-Modified", cacheEntry.getLastModified());
      }

      if (conf.getExpires().equals(ExpiresType.TIME)) {
        cacheEntry.setExpires(current + conf.getTime() * 1000);
        super.setDateHeader("Expires", cacheEntry.getExpires());
      }

      switch (conf.getMaxAgeType()) {

      case TIME:
        // set max-age based on life time
        cacheEntry.setMaxAge(-1 * (current + conf.getTime() * 1000));
        super.addHeader("Cache-Control", "max-age=" + conf.getTime());
        break;

      case NO_INIT:
        cacheEntry.setMaxAge(-1);
        break;

      default:
        cacheEntry.setMaxAge(conf.getMaxAge());
        super.addHeader("Cache-Control", "max-age=" + conf.getMaxAge());
      }

    }
  }

  private static long currentTime() {
    long current = System.currentTimeMillis();
    current = current - (current % 1000);
    return current;
  }

  public void setContentType(String value) {
    cacheEntry.setContentType(value);
    super.setContentType(value);
  }

  public void updateDateHeader(String name, long value) {

    if ((!conf.getLastModified().equals(LastModifiedType.OFF)) && ("Last-Modified".equalsIgnoreCase(name))) {
      if (!fragment) {
        cacheEntry.setLastModified(value);
      }
    }

    if ((!conf.getExpires().equals(ExpiresType.OFF)) && ("Expires".equalsIgnoreCase(name))) {
      if (!fragment) {
        cacheEntry.setExpires(value);
      }
    }
  }

  public void setDateHeader(String name, long value) {
    updateDateHeader(name, value);
    super.setDateHeader(name, value);
  }

  public void addDateHeader(String name, long value) {
    updateDateHeader(name, value);
    super.addDateHeader(name, value);
  }

  private void updateHeader(String name, String value) {
    if ("Content-Type".equalsIgnoreCase(name)) {
      cacheEntry.setContentType(value);
    }
    if ("Content-Encoding".equalsIgnoreCase(name)) {
      cacheEntry.setContentEncoding(value);
    }
  }

  public void setHeader(String name, String value) {
    updateHeader(name, value);
    super.setHeader(name, value);
  }

  public void addHeader(String name, String value) {
    updateHeader(name, value);
    super.addHeader(name, value);
  }

  public void setStatus(int status) {
    this.status = status;
    super.setStatus(status);
  }

  public void sendError(int status, String string) throws IOException {
    this.status = status;
    super.sendError(status, string);
  }

  public void sendError(int status) throws IOException {
    this.status = status;
    super.sendError(status);
  }

  public void setStatus(int status, String string) {
    this.status = status;
    super.setStatus(status, string);
  }

  public void sendRedirect(String location) throws IOException {
    this.status = SC_MOVED_TEMPORARILY;
    super.sendRedirect(location);
  }

  public int getStatus() {
    return status;
  }

  public void setLocale(Locale value) {
    cacheEntry.setLocale(value);
    super.setLocale(value);
  }

  public ServletOutputStream getOutputStream() throws IOException {
    if (out == null) {
      out = new CacheServletOutputStream(super.getOutputStream());
    }
    return out;
  }

  public PrintWriter getWriter() throws IOException {
    if (writer == null) {
      String encoding = getCharacterEncoding();
      if (encoding != null) {
        writer = new PrintWriter(new OutputStreamWriter(getOutputStream(), encoding));
      } else {
        writer = new PrintWriter(new OutputStreamWriter(getOutputStream()));
      }
    }
    return writer;
  }

  public void commit() throws IOException {
    super.flushBuffer();
    try {
      if (out != null) {
        out.flush();
      }
      if (writer != null) {
        writer.flush();
      }
    } catch (Exception e) {
      // ignore this exception
    }
    cacheEntry.setContent(out.toByteArray());
  }

}
