package com.mirantis.cachemod.filter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class DualBlockingLinkedList<E> {
  
  private final Entry<E> sentinel = new Entry<E>(null);
  private final Lock lock = new ReentrantLock();
  private final AtomicInteger size = new AtomicInteger(0);
  
  public static class Entry<E> {
    
    private static final AtomicReferenceFieldUpdater<Entry, Object> valueUpdater =
      AtomicReferenceFieldUpdater.newUpdater(Entry.class, Object.class, "value");
    
    private volatile Entry<E> next;
    private volatile Entry<E> prev;
    private volatile E value;
    
    public Entry(E initValue) {
      next = this;
      prev = this;
      value = initValue;
    }

    public E getValue() {
      return value;
    }

    public void setValue(E newValue) {
      valueUpdater.set(this, newValue);
    }
    
    private boolean isNew() {
      return next == this;
    }
    
    private void unlink() {
      prev.next = next;
      next.prev = prev;
      next = this;
      prev = this;
    }
    
    private void link(Entry<E> sentinel) {
      prev = sentinel.prev;
      prev.next = this;
      next = sentinel;
      sentinel.prev = this;
    }
    
  }
  
  public int size() {
    return size.get();
  }
  
  public void add(Entry<E> entry) {
    lock.lock();
    try {
      size.incrementAndGet();
      entry.link(sentinel);
    }
    finally {
      lock.unlock();
    }
  }
  
  public void touch(Entry<E> entry) {
    if (isTail(entry) || entry.isNew()) {
      return;
    }
    lock.lock();
    try {
      if (isTail(entry) || entry.isNew()) {
        return;
      }
      entry.unlink();
      entry.link(sentinel);
    } finally {
      lock.unlock();
    }
  }
  
  public void remove(Entry<E> entry) {
    while(true) {
      if (entry.isNew()) {
        continue; // wait addToTail
      }
      lock.lock();
      try {
        if (entry.isNew()) {
          continue;  // wait addToTail
        }
        size.decrementAndGet();
        entry.unlink();
      } finally {
        lock.unlock();
      }
      return;
    }
  }
  
  public Entry<E> first() {
    lock.lock();
    try {
      Entry<E> head = sentinel.next;
      if (head != sentinel) {
        size.decrementAndGet();
        head.unlink();
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
  
  private boolean isTail(Entry<E> entry) {
    return entry.next == sentinel;
  }
  
}
