package com.detector.CustomDataStructures;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * A lightweight queue implementation backed by a linked list.
 * Useful for when you need a simple FIFO structure without the overhead
 * of java.util.LinkedList.
 */
public class CustomQueue<T> implements Queue<T> {
    
    private class Node {
        T value;
        Node next;
        
        Node(T value) {
            this.value = value;
        }
    }

    private Node first;    // First element in the queue
    private Node last;     // Last element in the queue
    private int count;     // Number of elements currently in the queue

    public CustomQueue() {
        // Start empty
    }

    // Convenience constructor to create from existing collection
    public CustomQueue(Iterable<T> items) {
        if (items != null) {
            for (T item : items) {
                add(item);
            }
        }
    }

    @Override
    public boolean add(T item) {
        return offer(item);
    }

    @Override
    public boolean offer(T item) {
        Node newNode = new Node(item);
        
        if (last == null) {
            first = last = newNode;
        } else {
            last.next = newNode;
            last = newNode;
        }
        count++;
        return true;
    }

    @Override
    public T remove() {
        T value = poll();
        if (value == null) {
            throw new NoSuchElementException("Queue is empty");
        }
        return value;
    }

    @Override
    public T poll() {
        if (first == null) {
            return null;
        }

        T value = first.value;
        first = first.next;
        count--;

        if (first == null) {
            last = null;  // Queue is now empty
        }

        return value;
    }

    @Override
    public T element() {
        T value = peek();
        if (value == null) {
            throw new NoSuchElementException("Queue is empty");
        }
        return value;
    }

    @Override
    public T peek() {
        return first == null ? null : first.value;
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public boolean isEmpty() {
        return first == null;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node current = first;

            public boolean hasNext() {
                return current != null;
            }

            public T next() {
                if (current == null) {
                    throw new NoSuchElementException();
                }
                T value = current.value;
                current = current.next;
                return value;
            }
        };
    }

    @Override
    public boolean contains(Object item) {
        for (Node current = first; current != null; current = current.next) {
            if (item == null ? current.value == null : item.equals(current.value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[count];
        int i = 0;
        for (Node current = first; current != null; current = current.next) {
            result[i++] = current.value;
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> U[] toArray(U[] array) {
        if (array.length < count) {
            return (U[]) toArray();
        }
        
        int i = 0;
        for (Node current = first; current != null; current = current.next) {
            array[i++] = (U) current.value;
        }
        
        if (array.length > count) {
            array[count] = null;
        }
        return array;
    }

    @Override
    public boolean containsAll(Collection<?> items) {
        for (Object item : items) {
            if (!contains(item)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> items) {
        boolean changed = false;
        for (T item : items) {
            offer(item);
            changed = true;
        }
        return changed;
    }

    // Not supported operations - could implement these if needed
    @Override
    public boolean remove(Object item) {
        throw new UnsupportedOperationException("remove() not supported - use poll() instead");
    }

    @Override
    public boolean removeAll(Collection<?> items) {
        throw new UnsupportedOperationException("removeAll() not supported");
    }

    @Override
    public boolean retainAll(Collection<?> items) {
        throw new UnsupportedOperationException("retainAll() not supported");
    }

    @Override
    public void clear() {
        first = last = null;
        count = 0;
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        Node current = first;
        while (current.next != null) {
            sb.append(current.value).append(", ");
            current = current.next;
        }
        sb.append(current.value).append("]");
        return sb.toString();
    }
}