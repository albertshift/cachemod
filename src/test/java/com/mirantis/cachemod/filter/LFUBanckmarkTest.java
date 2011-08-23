package com.mirantis.cachemod.filter;

import junit.framework.TestCase;

public class LFUBanckmarkTest extends TestCase {

  public void testLRU() {
    LRUCacheProvider lru = new LRUCacheProvider();
    lru.init("cache");
    System.out.println("LRU = " + benchmark(lru));
  }

  public void testLFU() {
    LFUCacheProvider lfu = new LFUCacheProvider();
    lfu.init("cache");
    System.out.println("LFU = " + benchmark(lfu));
  }

  public void testNewLFU() {
    NewLFUCacheProvider lfu = new NewLFUCacheProvider();
    lfu.init("cache");
    System.out.println("NEW LFU = " + benchmark(lfu));
  }

  
  public long benchmark(CacheProvider provider) {
    long time0 = System.currentTimeMillis();
    CacheEntry entry = provider.instantiateEntry();
    for (int j = 0; j != 1000; ++j) {
      for (int i = 0; i != 1000; ++i) {
        entry = provider.instantiateEntry();
        String key = Integer.toString(j ^ i);
        provider.putEntry(key, entry);
        CacheEntry entryInCache = provider.getEntry(key);
        if (entryInCache != null) {
          assertEquals(entry, entryInCache);
        }
      }
    }
    return System.currentTimeMillis() - time0;
  }
  
}
