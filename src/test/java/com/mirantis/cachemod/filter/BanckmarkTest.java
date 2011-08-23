package com.mirantis.cachemod.filter;

import java.util.Random;

import junit.framework.TestCase;

public class BanckmarkTest extends TestCase {

  public void testLRU() {
    LRUCacheProvider lru = new LRUCacheProvider();
    lru.init("cache");
    //System.out.println("LRU = " + benchmark(lru));
    System.out.println("LRU_ND = " + benchmarkND(lru));
  }

  public void testSimpleLFU() {
    SimpleLFUCacheProvider lfu = new SimpleLFUCacheProvider();
    lfu.init("cache");
    //System.out.println("LFU = " + benchmark(lfu));
    System.out.println("Simple_LFU_ND = " + benchmarkND(lfu));
  }

  public void testLFU() {
    LFUCacheProvider lfu = new LFUCacheProvider();
    lfu.init("cache");
    //System.out.println("NEW_LFU = " + benchmark(lfu));
    System.out.println("LFU_ND = " + benchmarkND(lfu));
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

  public long benchmarkND(CacheProvider provider) {
    Random random = new Random();
    long time0 = System.currentTimeMillis();
    CacheEntry entry = provider.instantiateEntry();
    for (int j = 0; j != 1000000; ++j) {
      entry = provider.instantiateEntry();
      double norm = random.nextGaussian();
      norm = norm * 1;
      norm += 100000;
      long value = Math.round(norm);
      String key = Long.toString(value);

      //System.out.println(key);
      provider.putEntry(key, entry);
      CacheEntry entryInCache = provider.getEntry(key);
      if (entryInCache != null) {
        assertEquals(entry, entryInCache);
      }
    }
    System.out.println("size = " + provider.size());
    return System.currentTimeMillis() - time0;
  }

}
