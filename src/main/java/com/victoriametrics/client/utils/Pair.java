package com.victoriametrics.client.utils;

public class Pair<K, V> {

    private final K key;
    private final V Value;

    private Pair(K key, V value) {
        this.key = key;
        Value = value;
    }

    public static <K,V> Pair<K,V> of(K key, V value) {
        return new Pair<>(key, value);
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return Value;
    }
}
