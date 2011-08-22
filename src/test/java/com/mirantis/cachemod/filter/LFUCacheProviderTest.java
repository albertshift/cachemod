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

import junit.framework.TestCase;

import com.mirantis.cachemod.filter.LFUCacheProvider.LFUEntry;

public class LFUCacheProviderTest extends TestCase {

  public void testOne() {
    LFUCacheProvider lru = new LFUCacheProvider();
    lru.init("cache");
    CacheEntry entry = lru.instantiateEntry();
    lru.putEntry("1", entry);
    CacheEntry entryInCache = lru.getEntry("1");
    assertEquals(entry, entryInCache);
  }

  public void testEviction() {
    LFUCacheProvider lru = new LFUCacheProvider();
    lru.init("cache");
    CacheEntry entry = lru.instantiateEntry();
    for (int i = 0; i != 1000; ++i) {
      entry = lru.instantiateEntry();
      String key = Integer.toString(i);
      lru.putEntry(key, entry);
      CacheEntry entryInCache = lru.getEntry(key);
      assertEquals(entry, entryInCache);
    }
    ConcurrentHashMap<String, LFUEntry> cache = (ConcurrentHashMap<String, LFUEntry>) lru.getCache();
    entry = lru.instantiateEntry();
    lru.putEntry("1001", entry);
    assertEquals(1000, cache.size());
  }
  
  public void testSecondPut() {
    LFUCacheProvider lru = new LFUCacheProvider();
    lru.init("cache");
    ConcurrentHashMap<String, LFUEntry> cache = (ConcurrentHashMap<String, LFUEntry>) lru.getCache();

    
    CacheEntry entry = lru.instantiateEntry();
    lru.putEntry("1", entry);
    assertEquals(1, cache.size());

    entry = lru.instantiateEntry();
    lru.putEntry("11", entry);
    
    entry = lru.instantiateEntry();
    lru.putEntry("11", entry);
    assertEquals(2, cache.size());
    
    //lru.show();
  }

}