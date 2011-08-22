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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

public class CacheServletOutputStream extends ServletOutputStream {

  private transient ByteArrayOutputStream cacheStream = new ByteArrayOutputStream(1000);
  private OutputStream originalStream = null;

  public CacheServletOutputStream(OutputStream originalStream) {
    this.originalStream = originalStream;
  }

  public void write(int value) throws IOException {
    cacheStream.write(value);
    originalStream.write(value);
  }

  public void write(byte[] value) throws IOException {
    cacheStream.write(value);
    originalStream.write(value);
  }

  public void write(byte[] b, int off, int len) throws IOException {
    cacheStream.write(b, off, len);
    originalStream.write(b, off, len);
  }

  public void flush() throws IOException {
    super.flush();
    cacheStream.flush();
    originalStream.flush();
  }

  public void close() throws IOException {
    super.close();
    cacheStream.close();
    originalStream.close();
  }

  public byte[] toByteArray() {
    return cacheStream.toByteArray();
  }
  
}
