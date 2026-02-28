/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.olap.util.format.internal;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A thread-safe, bounded, Least-Recently-Used (LRU) cache.
 *
 * <p>
 * This cache uses a {@link LinkedHashMap} in access-order mode, wrapped with
 * {@link Collections#synchronizedMap(Map)} for thread safety. When the number
 * of entries exceeds the configured maximum capacity, the least recently
 * accessed entry is automatically evicted.
 *
 * <p>
 * The eviction strategy is based on
 * {@link LinkedHashMap#removeEldestEntry(Map.Entry)}. In access-order mode,
 * every {@code get} or {@code put} moves the accessed entry to the end of the
 * iteration order, so the entry at the head is always the least recently used.
 *
 * <p>
 * All public methods are synchronized via the wrapper map, which means
 * concurrent access is safe but serialized. This is appropriate for caches with
 * low contention, such as caching a small set of parsed format strings.
 *
 * <p>
 * Example usage:
 * 
 * <pre>
 * LruCache&lt;String, Format&gt; cache = new LruCache&lt;&gt;(1000);
 * Format f = cache.getOrCompute(key, k -&gt; new Format(formatString, locale));
 * </pre>
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of values
 */
public class LruCache<K, V> {

    private final Map<K, V> map;

    /**
     * Creates an LRU cache with the given maximum capacity.
     *
     * <p>
     * The initial capacity and load factor are chosen to avoid unnecessary
     * rehashing for typical usage patterns.
     *
     * @param maxCapacity the maximum number of entries before the least recently
     *                    used entry is evicted. Must be positive.
     * @throws IllegalArgumentException if maxCapacity is not positive
     */
    public LruCache(int maxCapacity) {
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("maxCapacity must be positive, was: " + maxCapacity);
        }
        // accessOrder=true makes LinkedHashMap order entries by last access,
        // so the head of the map is always the least recently used entry.
        this.map = Collections.synchronizedMap(new LinkedHashMap<>(64, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxCapacity;
            }
        });
    }

    /**
     * Returns the value associated with the given key, computing it if absent.
     *
     * <p>
     * If the key is already present, its value is returned and the entry is marked
     * as recently used. If absent, the mapping function is called to compute the
     * value, which is then stored in the cache.
     *
     * <p>
     * Note: the mapping function must not attempt to update this cache during
     * computation, as the underlying synchronized map does not support recursive
     * access.
     *
     * @param key             the key to look up
     * @param mappingFunction the function to compute the value if absent
     * @return the current (existing or computed) value associated with the key
     */
    public V getOrCompute(K key, Function<K, V> mappingFunction) {
        // Using synchronized block instead of Map.computeIfAbsent to
        // ensure the access-order update and the potential insertion
        // are atomic with respect to other threads.
        synchronized (map) {
            V value = map.get(key);
            if (value != null) {
                return value;
            }
            value = mappingFunction.apply(key);
            map.put(key, value);
            return value;
        }
    }

    /**
     * Returns the number of entries currently in the cache.
     *
     * @return the current size of the cache
     */
    public int size() {
        return map.size();
    }
}
