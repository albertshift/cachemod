package com.mirantis.cachemod.filter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HashMapCacheProvider implements CacheProvider {

  private final int DEF_UNITS = 1000;
  private final long DEF_FLASH = 60*1000;    // 60 seconds
  private final long DEF_EVICT = 60*60*1000; // 1 hour
  
  private Map<String, CacheEntry> map = new ConcurrentHashMap<String, CacheEntry>();
  private String cacheName;
  
  @Override
  public void init(String cacheName) {
    this.cacheName = cacheName;
  }

  @Override
  public CacheEntry instantiateEntry() { 
    return new CacheEntry();
  }

  @Override
  public CacheEntry getEntry(String key) {
    return map.get(key);
  }

  @Override
  public void putEntry(String key, CacheEntry cacheEntry) {
    map.put(key, cacheEntry);
  }

  @Override
  public Object getCache() {
    return this;
  }

}
