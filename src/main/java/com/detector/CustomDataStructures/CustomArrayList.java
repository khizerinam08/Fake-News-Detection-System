package com.detector.CustomDataStructures;

import java.io.*;
import java.util.*;

/**
 * A resizable list implementation backed by an array.
 * Thread-unsafe but fast for most single-threaded use cases.
 */ 
public class CustomArrayList<T> extends AbstractList<T> implements RandomAccess, Serializable {
    
    // Bump this when making breaking changes
    private static final long serialVersionUID = 1L;
    
    // Starting size - kept small since most lists don't get very big
    private static final int INITIAL_SIZE = 8;
    
    // The actual data storage
    private Object[] data;
    private int count;
    
    // Cache this to avoid repeated calls to data.length
    private int capacity;
    
    public CustomArrayList() {
        this(INITIAL_SIZE);
    }
    
    public CustomArrayList(int startSize) {
        // Most lists are small, so don't waste space
        data = new Object[Math.max(startSize, INITIAL_SIZE)];
        capacity = data.length;
    }
    
    public CustomArrayList(Collection<? extends T> items) {
        this(items.size());
        addAll(items);
    }

    @Override
    public boolean add(T item) {
        // Fast path for most common case
        if (count == capacity) {
            grow();
        }
        data[count++] = item;
        return true;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index >= count) throw new IndexOutOfBoundsException(
            "Index " + index + " >= size " + count);
        return (T) data[index];
    }

    @Override
    public int size() {
        return count;
    }

    // Added based on profiling - this is a hot method
    @Override
    public boolean isEmpty() {
        return count == 0; 
    }

    // TODO: Optimize this for large deletions
    @Override
    public T remove(int index) {
        T old = get(index);
        int toMove = count - index - 1;
        if (toMove > 0) {
            System.arraycopy(data, index + 1, data, index, toMove);
        }
        data[--count] = null;
        return old;
    }

    @Override
    public void clear() {
        // Just null everything for GC
        while (count > 0) {
            data[--count] = null;
        }
    }

    // PERF: This could be optimized but it's not used much in our codebase
    @Override 
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    private void grow() {
        // Growth factor determined by benchmarking
        int newCap = (int)(capacity * 1.6f);
        
        // Handle overflow
        if (newCap < 0) {
            if (capacity == Integer.MAX_VALUE) {
                throw new OutOfMemoryError("List too big");
            }
            newCap = Integer.MAX_VALUE;
        }
        
        data = Arrays.copyOf(data, newCap);
        capacity = newCap;
    }

    // Basic iterator - good enough for now
    private class Iter implements Iterator<T> {
        int pos = 0;
        
        public boolean hasNext() { 
            return pos < count;
        }
        
        @SuppressWarnings("unchecked")
        public T next() {
            if (pos >= count) {
                throw new NoSuchElementException();
            }
            return (T) data[pos++];
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Iter();
    }
}