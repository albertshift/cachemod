/*
 * Copyright 2011 Mirantis Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mirantis.cachemod.filter;

import java.util.concurrent.ConcurrentHashMap;

public class NewLFUCacheProvider implements CacheProvider {

  private final int UNITS = Integer.getInteger("cachemod.lfu.units", 1000);
  private final int CONCURRENT = Integer.getInteger("cachemod.lfu.concurrent", 16);

  private ConcurrentHashMap<String, LFUEntry> localMap;
  private String cacheName;

  private DualBlockingSortedList<CacheEntry> list = new DualBlockingSortedList<CacheEntry>();  

  public static class LFUEntry extends DualBlockingSortedList.Entry<CacheEntry> {
   
    private final String key;
    
    public LFUEntry(String key, CacheEntry initValue) {
      super(initValue);
      this.key = key;
    }

    public String getKey() {
      return key;
    }

  }
  
  @Override
  public void init(String cacheName) {
    this.cacheName = cacheName;
    this.localMap = new ConcurrentHashMap<String, LFUEntry>(UNITS, 0.75f, CONCURRENT);
  }

  @Override
  public CacheEntry instantiateEntry() {
    return new CacheEntry();
  }

  @Override
  public CacheEntry getEntry(String key) {
    LFUEntry entry = localMap.get(key);
    if (entry != null) {
      list.touch(entry);
      return entry.getValue();
    }
    return null;
  }

  @Override
  public void putEntry(String key, CacheEntry cacheEntry) {
    LFUEntry newEntry = new LFUEntry(key, cacheEntry);
    LFUEntry prevEntry = localMap.putIfAbsent(key, newEntry);
    if (prevEntry != null) {
      prevEntry.setValue(cacheEntry);
      list.touch(prevEntry);
    }
    else {
      list.add(newEntry);
      evict();
    }
  }

  public void evict() {
    while(list.size() > UNITS) {
      LFUEntry entry = (LFUEntry) list.first();
      if (entry != null) {
        localMap.remove(entry.getKey());
      }
    }
  }
  
  @Override
  public Object getCache() {
    return localMap;
  }

  public String getCacheName() {
    return cacheName;
  }
  
}