package com.mirantis.coherence.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Locale;

import com.mirantis.cachemod.filter.CacheEntry;
import com.mirantis.cachemod.filter.CoherenceCacheEntry;
import com.tangosol.io.ByteArrayReadBuffer;
import com.tangosol.io.ByteArrayWriteBuffer;
import com.tangosol.io.pof.PofBufferReader;
import com.tangosol.io.pof.PofBufferWriter;
import com.tangosol.io.pof.PofContext;

public class TestUtils {

  public static byte[] serializeByJava(Object obj) throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(bout);
    out.writeObject(obj);
    out.close();
    return bout.toByteArray();
  }
  
  public static byte[] serializeByPof(Object obj, PofContext pofContext) throws IOException {
    ByteArrayWriteBuffer buf = new ByteArrayWriteBuffer(10000);
    PofBufferWriter writer = new PofBufferWriter(buf.getBufferOutput(), pofContext);
    writer.writeObject(0, obj);
    return buf.toByteArray();
  }
  
  public static Object deserializeByPof(byte[] pof, PofContext pofContext) throws IOException {
    ByteArrayReadBuffer buf = new ByteArrayReadBuffer(pof);
    PofBufferReader reader = new PofBufferReader(buf.getBufferInput(), pofContext);
    return reader.readObject(0);
  }
  
  public static CacheEntry createCacheEntry(String responseText) throws IOException {
    CacheEntry content = new CoherenceCacheEntry();
    content.setContentEncoding("UTF-8");
    content.setContentType("text/html");
    content.setExpires(-1);
    content.setLastModified(System.currentTimeMillis());
    content.setLocale(new Locale("en_US"));
    content.setMaxAge(-1);
    content.setContent("content".getBytes());
    return content;
  }
  

  
}
