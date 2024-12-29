package com.detector.CustomDataStructures;

import java.util.*;

// Fast hash-based map implementation optimized for object keys
public class CustomHashMap<K,V> extends AbstractMap<K,V> {
    
    private static final int INIT_SIZE = 32;
    private static final float MAX_LOAD = 0.65f;
    
    // Simple node class - kept minimal for memory reasons
    private static class Node<K,V> {
        final K key;  // final because keys shouldn't change
        V val;
        Node<K,V> next;
        
        Node(K k, V v, Node<K,V> n) {
            key = k;
            val = v; 
            next = n;
        }
    }
    
    private Node<K,V>[] bins;
    private int items;  // Number of items stored
    private int modCount;  // For fail-fast iteration
    
    // Cache these calculations
    private int resizeAt;
    
    @SuppressWarnings("unchecked")
    public CustomHashMap() {
        bins = new Node[INIT_SIZE];
        resizeAt = (int)(INIT_SIZE * MAX_LOAD);
    }
    
    @Override 
    public V get(Object key) {
        Node<K,V> n = bins[index(key)];
    
        while (n != null) {
            if (n.key == key || n.key.equals(key)) {
                return n.val;
            }
            n = n.next;
        }
        return null;
    }
    
    @Override
    public V put(K key, V val) {
        if (key == null) {
            throw new NullPointerException("Null keys not allowed");
        }
        
        int idx = index(key);
        
        // Check for existing key
        for (Node<K,V> n = bins[idx]; n != null; n = n.next) {
            if (n.key == key || n.key.equals(key)) {
                V old = n.val;
                n.val = val;
                return old;
            }
        }
        
        // No existing key found - add new node
        modCount++;
        addNode(key, val, idx);
        
        if (++items > resizeAt) {
            // Double size and rehash
            resize();
        }
        
        return null;
    }
    
    private void addNode(K key, V val, int idx) {
        // Always add at head - slightly faster
        bins[idx] = new Node<>(key, val, bins[idx]);
    }
    
    private int index(Object key) {
        return (key.hashCode() & 0x7fffffff) % bins.length;
    }
    
    @SuppressWarnings("unchecked")
    private void resize() {
        Node<K,V>[] old = bins;
        int newSize = old.length * 2;
        
        // Check for integer overflow
        if (newSize < 0) {
            if (old.length == Integer.MAX_VALUE) {
                throw new IllegalStateException("Map too big!");
            }
            newSize = Integer.MAX_VALUE;
        }
        
        bins = new Node[newSize];
        resizeAt = (int)(newSize * MAX_LOAD);
        
        // Move all nodes to new bins
        for (Node<K,V> node : old) {
            while (node != null) {
                Node<K,V> next = node.next;
                int idx = index(node.key);
                node.next = bins[idx];
                bins[idx] = node;
                node = next;
            }
        }
    }
    
    @Override
    public int size() { 
        return items; 
    }
    
    @Override
    public Set<Map.Entry<K,V>> entrySet() {
        Set<Map.Entry<K,V>> set = new HashSet<>();
        int mc = modCount;
        
        for (Node<K,V> node : bins) {
            while (node != null) {
                set.add(new SimpleEntry<>(node.key, node.val));
                node = node.next;
            }
        }
        
        // Check if map was modified during iteration
        if (mc != modCount) {
            throw new ConcurrentModificationException();
        }
        
        return set;
    }
}