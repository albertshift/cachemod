package com.mirantis.cachemod.filter;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;

public class GemFireCacheProvider implements CacheProvider {

  private Cache cache;
  private Region<String, GemFireCacheEntry> region;
  
  @Override
  public void init(String cacheName) {
    cache = CacheFactory.getAnyInstance();
    region = cache.getRegion(cacheName);   
  }

  @Override
  public CacheEntry instantiateEntry() {
    return new GemFireCacheEntry();
  }

  @Override
  public CacheEntry getEntry(String key) {
    return region.get(key);
  }

  @Override
  public void putEntry(String key, CacheEntry cacheEntry) {
    region.put(key, (GemFireCacheEntry) cacheEntry);
  }

  @Override
  public Object getCache() {
    return region;
  }

}
