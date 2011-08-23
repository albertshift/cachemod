package com.mirantis.cachemod.filter;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class DualBlockingSortedList<E> {
  
  private final Node<E> head = new Node<E>();
  private final Lock lock = new ReentrantLock();
  private volatile int size = 0;
  
  private void touchSingleEntryNode(Node<E> node, Entry<E> entry) {
    Node<E> nextNode = node.next;
    if (nextNode == null) {
      /*
       * This is the last node, move forward
       */
      node.moveForward();
      return;
    }
    if (nextNode.hasGhosts()) {
      /*
       * We could go forward, we have space
       */
      nextNode.eatGhost();
      node.moveForward();
      return;
    }
    if (nextNode.isEmpty()) {
      /*
       * Eat empty next node and move forward
       */
      nextNode.removeAfter(node);
      node.moveForward();
      return;
    }
   /*
     * Move entry to next node
     */
    node.removeEntry(entry);
    nextNode.addEntry(entry);
    /*
     * Node became empty, it will be collected at the next time
     */
  }
  
  private void touchNode(Node<E> node, Entry<E> entry) {
    Node<E> nextNode = node.next;
    if (nextNode == null) {
      /*
       * This is the last node, create new node and move entry to new node
       */
      nextNode = new Node<E>();
      nextNode.insertAfter(node);
      node.removeEntry(entry);
      nextNode.addEntry(entry);
      return;
    }
    if (nextNode.hasGhosts()) {
      /*
       * Magic one ghost to real node
       */
      nextNode.eatGhost();
      Node<E> middleNode = new Node<E>();
      middleNode.insertAfter(node);
      node.removeEntry(entry);
      middleNode.addEntry(entry);
      return;
    }
    if (nextNode.isEmpty()) {
      node.removeEntry(entry);
      nextNode.addEntry(entry);
      return;
    }
    /*
     * Move entry to next node
     */
    node.removeEntry(entry);
    nextNode.addEntry(entry);
  }
  
  public void touch(Entry<E> entry) {
    if (entry.isNew()) {
      return;
    }
    lock.lock();
    try {
      if (entry.isNew()) {
        return;
      }
      Node<E> node = entry.node;
      if (node.hasSingleEntry()) {
        touchSingleEntryNode(node, entry);
      }
      else {
        touchNode(node, entry);
      }
    }
    finally {
      lock.unlock();
    }
  }
  
  public void add(Entry<E> entry) {
    lock.lock();
    try {
      head.addEntry(entry);
      size++;
    }
    finally {
      lock.unlock();
    }
  }
  
  public Entry<E> first() {
    lock.lock();
    try {
      Node<E> node = head;
      while(node != null) {
        Entry<E> entry = node.sentinel.next;
        while(entry.isNew() && entry != node.sentinel) {
          entry = entry.next;
        }
        if (entry != node.sentinel) {
          size--;
          node.removeEntry(entry);
          return entry;
        }
        node = node.next;
      }
      return null;
    }
    finally {
      lock.unlock();
    }
    
  }
  
  public void remove(Entry<E> entry) {
    while(true) {
      if (!entry.isNew()) {
        lock.lock();
        try {
          if (entry.isNew()) {
            continue;  // wait addEntry
          }
          size--;
          entry.node.removeEntry(entry);
        } finally {
          lock.unlock();
        }
        return;
      }
      try {
        Thread.currentThread().sleep(1);
      }
      catch(Exception ignore) {
      }
    }
  }

  public int size() {
    return size;
  }

  public static class Node<E> {

    private volatile Node<E> next = null;
    private final Entry<E> sentinel = new Entry<E>(null);
    private volatile int entries;
    private volatile int ghostsBefore;

    public boolean isEmpty() {
      return entries == 0;
    }
    
    public boolean hasSingleEntry() {
      return entries == 1;
    }
    
    public boolean hasGhosts() {
      return ghostsBefore > 0;
    }
    
    public void moveForward() {
      ghostsBefore++;
    }
    
    public void eatGhost() {
      ghostsBefore--;
    }
    
    public void addEntry(Entry<E> entry) {
      entry.link(this);
      entries++;
    }
    
    public void removeEntry(Entry<E> entry) {
      entry.unlink();
      entries--;
    }

    public void insertAfter(Node<E> prevNode) {
      next = prevNode.next;
      prevNode.next = this;
    }
    
    public void removeAfter(Node<E> prevNode) {
      prevNode.next = next;
      next = null;
    }
  }
  
  public static class Entry<E> {
    
    private static final AtomicReferenceFieldUpdater<Entry, Object> valueUpdater =
      AtomicReferenceFieldUpdater.newUpdater(Entry.class, Object.class, "value");
    
    private volatile Node<E> node; 
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
      node = null;
    }
    
    private void link(Node<E> node) {
      this.node = node;
      Entry<E> sentinel = node.sentinel;
      prev = sentinel.prev;
      prev.next = this;
      next = sentinel;
      sentinel.prev = this;
    }
    
  }

}
