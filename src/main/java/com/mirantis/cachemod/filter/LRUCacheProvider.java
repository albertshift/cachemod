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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LRUCacheProvider implements CacheProvider {

  private final int UNITS = Integer.getInteger("cachemod.lru.units", 1000);
  private final int CONCURRENT = Integer.getInteger("cachemod.lru.concurrent", 16);

  private ConcurrentHashMap<String, LRUCacheEntry> localMap;
  private String cacheName;
  private LRUCacheEntry point = new LRUCacheEntry();
  private Lock lock = new ReentrantLock();

  public class LRUCacheEntry extends CacheEntry {

    private static final long serialVersionUID = 34214312L;

    private volatile LRUCacheEntry next;
    private volatile LRUCacheEntry prev;

    public LRUCacheEntry() {
      next = this;
      prev = this;
    }
    
    public boolean isNew() {
      return next == this;
    }
    
    public void addToTail() {
      lock.lock();
      try {
        logicAddToTail();
      }
      finally {
        lock.unlock();
      }
    }
    
    public LRUCacheEntry removeHead() {
      lock.lock();
      try {
        LRUCacheEntry head = point.next;
        if (head != point) {
          head.logicRemove();
          return head;
        }
        else {
          return null;
        }
      }
      finally {
        lock.unlock();
      }
    }
    
    public void remove() {
      while(true) {
        if (isNew()) {
          continue; // wait addToTail
        }
        lock.lock();
        try {
          if (isNew()) {
            continue;  // wait addToTail
          }
          logicRemove();
        } finally {
          lock.unlock();
        }
        return;
      }
    }
    
    public void moveToTail() {
      if (isTail() || isNew()) {
        return;
      }
      lock.lock();
      try {
        if (isTail() || isNew()) {
          return;
        }
        logicRemove();
        logicAddToTail();
      } finally {
        lock.unlock();
      }
    }
    
    public boolean isTail() {
      return next == point;
    }
    
    private void logicRemove() {
      prev.next = next;
      next.prev = prev;
      next = this;
      prev = this;
    }
    
    private void logicAddToTail() {
      prev = point.prev;
      prev.next = this;
      next = point;
      point.prev = this;
    }
    
    public void show() {
      LRUCacheEntry go = next;
      while(go != this) {
        System.out.println("GO = " + go.getKey() + go);
        go = go.next;
      }
    }
    
  }
  
  @Override
  public void init(String cacheName) {
    this.cacheName = cacheName;
    this.localMap = new ConcurrentHashMap<String, LRUCacheEntry>(UNITS, 0.75f, CONCURRENT);
  }

  @Override
  public CacheEntry instantiateEntry() {
    return new LRUCacheEntry();
  }

  @Override
  public CacheEntry getEntry(String key) {
    LRUCacheEntry lruCacheEntry = localMap.get(key);
    if (lruCacheEntry != null) {
      lruCacheEntry.moveToTail();
    }
    return lruCacheEntry;
  }

  @Override
  public void putEntry(String key, CacheEntry cacheEntry) {
    LRUCacheEntry lruCacheEntry = (LRUCacheEntry) cacheEntry;
    LRUCacheEntry prev = localMap.put(key, lruCacheEntry);
    if (prev != null) {
      prev.remove();
    }
    if (lruCacheEntry.isNew()) {
      lruCacheEntry.addToTail();
    }
    else {
      lruCacheEntry.moveToTail();
    }
    evict();
  }

  public void evict() {
    while(localMap.size() > UNITS) {
      LRUCacheEntry head = point.removeHead();
      if (head != null) {
        localMap.remove(head.getKey());
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

  public void show() {
    point.show();
  }
  
}
