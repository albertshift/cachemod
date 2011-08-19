package com.mirantis.cachemod.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public class CoherenceCacheProvider implements CacheProvider {

  private static final Log log = LogFactory.getLog(CoherenceCacheProvider.class);
  
  private NamedCache cache = null;
  
  public void init(String cacheName) {
    cache = CacheFactory.getCache(cacheName);
  }
  
  @Override
  public CacheEntry instantiateEntry() {
    return new CoherenceCacheEntry();
  }
  
  public CacheEntry getEntry(String key) {
    try {
      return (CacheEntry) cache.get(key);
    }
    catch(Exception e) {
      log.error("fail to get page from cache " + key, e);
    }
    return null;
  }

  public void putEntry(String key, CacheEntry cacheEntry) {
    try {
      cache.put(key, cacheEntry);
    }
    catch(Exception e) {
      log.error("fail to put page to cache " + key, e);
    }
  }
  
  public Object getCache() {
    return cache;
  }
  
}
