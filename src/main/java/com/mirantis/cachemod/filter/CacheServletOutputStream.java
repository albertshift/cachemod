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
