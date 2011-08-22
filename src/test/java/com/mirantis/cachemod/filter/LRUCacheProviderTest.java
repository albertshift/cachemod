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

import com.mirantis.cachemod.filter.LRUCacheProvider.LRUCacheEntry;

import junit.framework.TestCase;

public class LRUCacheProviderTest extends TestCase {

  public void testOne() {
    LRUCacheProvider lru = new LRUCacheProvider();
    lru.init("cache");
    CacheEntry entry = lru.instantiateEntry();
    entry.setKey("1");
    lru.putEntry("1", entry);
    CacheEntry entryInCache = lru.getEntry("1");
    assertEquals(entry, entryInCache);
  }

  public void testEviction() {
    LRUCacheProvider lru = new LRUCacheProvider();
    lru.init("cache");
    CacheEntry entry = lru.instantiateEntry();
    for (int i = 0; i != 1000; ++i) {
      entry = lru.instantiateEntry();
      String key = Integer.toString(i);
      entry.setKey(key);
      lru.putEntry(key, entry);
      CacheEntry entryInCache = lru.getEntry(key);
      assertEquals(entry, entryInCache);
    }
    ConcurrentHashMap<String, LRUCacheEntry> cache = (ConcurrentHashMap<String, LRUCacheEntry>) lru.getCache();
    entry = lru.instantiateEntry();
    entry.setKey("1001");
    lru.putEntry("1001", entry);
    assertEquals(1000, cache.size());
  }
  
  public void testSecondPut() {
    LRUCacheProvider lru = new LRUCacheProvider();
    lru.init("cache");
    ConcurrentHashMap<String, LRUCacheEntry> cache = (ConcurrentHashMap<String, LRUCacheEntry>) lru.getCache();

    
    CacheEntry entry = lru.instantiateEntry();
    entry.setKey("1");
    lru.putEntry("1", entry);
    assertEquals(1, cache.size());

    entry = lru.instantiateEntry();
    entry.setKey("11");
    lru.putEntry("11", entry);
    
    entry = lru.instantiateEntry();
    entry.setKey("11");
    lru.putEntry("11", entry);
    assertEquals(2, cache.size());
    
    //lru.show();
  }

}
