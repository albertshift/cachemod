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

import com.mirantis.cachemod.filter.SimpleLFUCacheProvider.LFUEntry;

public class LFUCacheProviderTest extends TestCase {

  public void testOne() {
    SimpleLFUCacheProvider lfu = new SimpleLFUCacheProvider();
    lfu.init("cache");
    CacheEntry entry = lfu.instantiateEntry();
    lfu.putEntry("1", entry);
    CacheEntry entryInCache = lfu.getEntry("1");
    assertEquals(entry, entryInCache);
  }

  public void testEviction() {
    SimpleLFUCacheProvider lfu = new SimpleLFUCacheProvider();
    lfu.init("cache");
    CacheEntry entry = lfu.instantiateEntry();
    for (int i = 0; i != 1000; ++i) {
      entry = lfu.instantiateEntry();
      String key = Integer.toString(i);
      lfu.putEntry(key, entry);
      CacheEntry entryInCache = lfu.getEntry(key);
      assertEquals(entry, entryInCache);
    }
    ConcurrentHashMap<String, LFUEntry> cache = (ConcurrentHashMap<String, LFUEntry>) lfu.getCache();
    entry = lfu.instantiateEntry();
    lfu.putEntry("1001", entry);
    assertEquals(1000, cache.size());
  }
  
  public void testSecondPut() {
    SimpleLFUCacheProvider lfu = new SimpleLFUCacheProvider();
    lfu.init("cache");
    ConcurrentHashMap<String, LFUEntry> cache = (ConcurrentHashMap<String, LFUEntry>) lfu.getCache();

    
    CacheEntry entry = lfu.instantiateEntry();
    lfu.putEntry("1", entry);
    assertEquals(1, cache.size());

    entry = lfu.instantiateEntry();
    lfu.putEntry("11", entry);
    
    entry = lfu.instantiateEntry();
    lfu.putEntry("11", entry);
    assertEquals(2, cache.size());
    
    //lru.show();
  }

}