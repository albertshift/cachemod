package com.mirantis.cachemod.filter;

public interface CacheProvider {

  public void init(String cacheName); 
  
  public CacheEntry instantiateEntry();
  
  public CacheEntry getEntry(String key);
  
  public void putEntry(String key, CacheEntry cacheEntry);
  
  public Object getCache();
  
}
