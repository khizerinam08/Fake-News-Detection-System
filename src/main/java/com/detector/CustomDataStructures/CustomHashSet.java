package com.detector.CustomDataStructures;

import java.util.*;

// Fast set implementation using open addressing with linear probing
// Warning: Not thread-safe, use Collections.synchronizedSet() if needed
public class CustomHashSet<E> extends AbstractSet<E> {
    
    // Benchmarked these values - better than traditional 16/0.75
    private static final int START_SIZE = 32;
    private static final float GROW_AT = 0.6f;  // Less collisions than 0.75
    
    // Special marker for deleted slots
    private static final Object TOMBSTONE = new Object();
    
    private Object[] data;  // The actual storage
    private int used = 0;   // Count of used slots (including tombstones)
    private int size = 0;   // Actual number of elements
    
    // For fail-fast iteration
    private int mods = 0;
    
    public CustomHashSet() {
        data = new Object[START_SIZE];
    }
    
    @Override
    public boolean add(E e) {
        if (e == null) 
            throw new NullPointerException();
            
        // PERF: Could special-case primitive wrappers
        if (contains(e))
            return false;
            
        if (used > data.length * GROW_AT) {
            rehash();
        }
        
        int idx = findSlot(e);
        if (data[idx] == null || data[idx] == TOMBSTONE) {
            data[idx] = e;
            size++;
            used++;
            mods++;
            return true;
        }
        
        // Should never happen if contains() worked correctly
        throw new IllegalStateException("Logic error in add()");
    }

    // Main lookup method - optimized for speed
    @Override
    public boolean contains(Object o) {
        if (o == null)
            return false;
            
        // Try to find the element
        int idx = Math.abs(o.hashCode() % data.length);
        int startIdx = idx;
        
        do {
            Object existing = data[idx];
            
            // Empty slot means element not found
            if (existing == null)
                return false;
                
            // Skip tombstones
            if (existing != TOMBSTONE) {
                // Identity comparison first - often faster
                if (existing == o || existing.equals(o))
                    return true;
            }
            
            // Linear probe
            idx = (idx + 1) % data.length;
        } while (idx != startIdx);
        
        return false;
    }
    
    @Override
    public boolean remove(Object o) {
        if (o == null)
            return false;
            
        int idx = findElement(o);
        if (idx != -1) {
            data[idx] = TOMBSTONE;
            size--;
            mods++;
            return true;
        }
        return false;
    }
    
    // TODO: Optimize this for large sets
    private int findSlot(Object o) {
        int idx = Math.abs(o.hashCode() % data.length);
        while (data[idx] != null && data[idx] != TOMBSTONE) {
            idx = (idx + 1) % data.length;
        }
        return idx;
    }
    
    private int findElement(Object o) {
        int idx = Math.abs(o.hashCode() % data.length);
        int startIdx = idx;
        
        do {
            Object existing = data[idx];
            if (existing == null)
                return -1;
            if (existing != TOMBSTONE && (existing == o || existing.equals(o)))
                return idx;
            idx = (idx + 1) % data.length;
        } while (idx != startIdx);
        
        return -1;
    }
    
    @SuppressWarnings("unchecked")
    private void rehash() {
        Object[] old = data;
        // Double size, but check for overflow
        int newSize = old.length * 2;
        if (newSize < 0) {
            if (old.length == Integer.MAX_VALUE) {
                throw new IllegalStateException("Set too big!");
            }
            newSize = Integer.MAX_VALUE;
        }
        
        data = new Object[newSize];
        size = 0;
        used = 0;
        
        // Reinsert all elements
        for (Object e : old) {
            if (e != null && e != TOMBSTONE) {
                add((E)e);  // Safe cast since we only put E elements in
            }
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private int index = -1;
            private int remaining = size;
            private final int expectedMods = mods;
            
            @Override
            public boolean hasNext() {
                return remaining > 0;
            }
            
            @Override
            @SuppressWarnings("unchecked")
            public E next() {
                if (expectedMods != mods)
                    throw new ConcurrentModificationException();
                    
                if (!hasNext())
                    throw new NoSuchElementException();
                    
                while (++index < data.length) {
                    Object e = data[index];
                    if (e != null && e != TOMBSTONE) {
                        remaining--;
                        return (E)e;
                    }
                }
                // Should never happen if hasNext() works
                throw new IllegalStateException();
            }
        };
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public void clear() {
        Arrays.fill(data, null);
        size = 0;
        used = 0;
        mods++;
    }
}