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

import java.io.Serializable;
import java.util.Locale;

public class CacheEntry implements Serializable {
  
  private static final long serialVersionUID = 3463262L;
  
  protected String key = null;
  protected Object userData = null;
  protected Locale locale = null;
  protected long expires = Long.MAX_VALUE;
  protected long lastModified = -1;
  protected long maxAge = 60;
  protected String contentEncoding = null;
  protected String contentType = null;
  protected byte[] content = null;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String value) {
    contentType = value;
  }

  public long getLastModified() {
    return lastModified;
  }

  public void setLastModified(long value) {
    lastModified = value;
  }

  public String getContentEncoding() {
    return contentEncoding;
  }

  public void setContentEncoding(String contentEncoding) {
    this.contentEncoding = contentEncoding;
  }

  public void setLocale(Locale value) {
    locale = value;
  }

  public long getExpires() {
    return expires;
  }

  public void setExpires(long value) {
    expires = value;
  }

  public long getMaxAge() {
    return maxAge;
  }

  public void setMaxAge(long value) {
    maxAge = value;
  }

  public byte[] getContent() {
    return content;
  }

  public void setContent(byte[] content) {
    this.content = content;
  }

  public Locale getLocale() {
    return locale;
  }

  public Object getUserData() {
    return userData;
  }

  public void setUserData(Object userData) {
    this.userData = userData;
  }

}
