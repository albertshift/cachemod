package com.mirantis.cachemod.filter;

import java.util.Random;

import junit.framework.TestCase;

public class BanckmarkTest extends TestCase {

  public void test() {
    
  }
  
  public void notestLRU() {
    LRUCacheProvider lru = new LRUCacheProvider();
    lru.init("cache");
    //System.out.println("LRU = " + benchmark(lru));
    System.out.println("LRU_ND = " + benchmarkND(lru));
  }

  public void notestLFU() {
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
    random.setSeed(123456789);
    CacheEntry entry = provider.instantiateEntry();
    long hits = 0;
    boolean[] hitsArray = new boolean[100000];
    long time0 = System.currentTimeMillis();
    for (int j = 0; j != 10000000; ++j) {
      double norm = random.nextGaussian() * 10000000;
      norm += hitsArray.length;
      long value = Math.round(norm);
      String key = Long.toString(value);

      if (value >= 0 && value < hitsArray.length) {
        hitsArray[(int)value] = true;
      }

      CacheEntry entryInCache = provider.getEntry(key);
      if (entryInCache != null) {
        hits++;
        //assertEquals(entry, entryInCache);
      }
      else {
        //System.out.println(key);
        provider.putEntry(key, entry);
      }
    }
    long time = System.currentTimeMillis() - time0;
    int effectiveSize = 0;
    for (int i = 0; i != hitsArray.length; ++i) {
      if (hitsArray[i]) {
        effectiveSize++;
      }
    }
    
    System.out.println("size = " + provider.size());
    System.out.println("effectiveSize = " + effectiveSize);
    System.out.println("hits = " + (double)hits/100000 + "%");
    return time;
  }

}
