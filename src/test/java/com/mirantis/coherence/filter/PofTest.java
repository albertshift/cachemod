package com.mirantis.coherence.filter;

import junit.framework.TestCase;

import com.mirantis.cachemod.filter.CacheEntry;
import com.tangosol.io.pof.ConfigurablePofContext;

public class PofTest extends TestCase {

  public void test() throws Exception {
    
    ConfigurablePofContext pofContext = new ConfigurablePofContext("coherence-filter-pof-config.xml");    
    
    CacheEntry cacheEntry = TestUtils.createCacheEntry("responseText");
  
    byte[] contentPof = TestUtils.serializeByPof(cacheEntry, pofContext);
    CacheEntry cacheEntryCheck = (CacheEntry) TestUtils.deserializeByPof(contentPof, pofContext);
    assertEquals(cacheEntry, cacheEntryCheck);

  }
 

  public static void assertEquals(CacheEntry cacheEntry, CacheEntry cacheEntryCheck) throws Exception {
    assertEquals(cacheEntry.getContentEncoding(), cacheEntryCheck.getContentEncoding());
    assertEquals(cacheEntry.getContentType(), cacheEntryCheck.getContentType());
    assertEquals(cacheEntry.getExpires(), cacheEntryCheck.getExpires());
    assertEquals(cacheEntry.getLastModified(), cacheEntryCheck.getLastModified());
    assertEquals(cacheEntry.getMaxAge(), cacheEntryCheck.getMaxAge());
    assertEquals(cacheEntry.getContent().length, cacheEntryCheck.getContent().length);
  }

}
