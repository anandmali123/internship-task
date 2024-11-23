import java.util.concurrent.*;
import java.util.*;

class Cache<K, V> {
    private final ConcurrentHashMap<K, CacheEntry<V>> cacheMap = new ConcurrentHashMap<>();
    private final PriorityQueue<ExpiryEntry<K>> expiryQueue = new PriorityQueue<>(Comparator.comparingLong(e -> e.expiryTime));
    private final ScheduledExecutorService cleaner = Executors.newScheduledThreadPool(1);

    public Cache() {
        cleaner.scheduleAtFixedRate(this::cleanUp, 1, 1, TimeUnit.SECONDS);
    }

    // Create or Update a key-value pair with TTL
    public void put(K key, V value, long ttl) {
        long expiryTime = System.currentTimeMillis() + ttl;
        CacheEntry<V> newEntry = new CacheEntry<>(value, expiryTime);
        cacheMap.put(key, newEntry);
        synchronized (expiryQueue) {
            expiryQueue.add(new ExpiryEntry<>(key, expiryTime));
        }
        System.out.println("Added/Updated: " + key + " => " + value);
    }

    // Read a value by key
    public V get(K key) {
        CacheEntry<V> entry = cacheMap.get(key);
        if (entry == null || System.currentTimeMillis() > entry.expiryTime) {
            cacheMap.remove(key);
            return null;
        }
        return entry.value;
    }

    // Delete a key from the cache
    public void delete(K key) {
        cacheMap.remove(key);
        System.out.println("Deleted: " + key);
    }

    // Update an existing key's TTL or value
    public void update(K key, V newValue, long newTTL) {
        if (cacheMap.containsKey(key)) {
            put(key, newValue, newTTL);
            System.out.println("Updated: " + key + " with new value and TTL.");
        } else {
            System.out.println("Key not found: " + key);
        }
    }

    // Background cleanup of expired keys
    private void cleanUp() {
        long currentTime = System.currentTimeMillis();
        synchronized (expiryQueue) {
            while (!expiryQueue.isEmpty() && expiryQueue.peek().expiryTime <= currentTime) {
                K key = expiryQueue.poll().key;
                cacheMap.remove(key);
                System.out.println("Expired and removed: " + key);
            }
        }
    }

    // Stop the cleaner thread
    public void stopCleaner() {
        cleaner.shutdown();
    }

    // Cache entry data structure
    private static class CacheEntry<V> {
        V value;
        long expiryTime;

        CacheEntry(V value, long expiryTime) {
            this.value = value;
            this.expiryTime = expiryTime;
        }
    }

    // Expiry tracking data structure
    private static class ExpiryEntry<K> {
        K key;
        long expiryTime;

        ExpiryEntry(K key, long expiryTime) {
            this.key = key;
            this.expiryTime = expiryTime;
        }
    }
}

public class CustomCacheWithExpiry {
    public static void main(String[] args) throws InterruptedException {
        Cache<String, String> cache = new Cache<>();

        // Create entries with TTL
        cache.put("key1", "value1", 5000); // Expires in 5 seconds
        cache.put("key2", "value2", 3000); // Expires in 3 seconds

        // Read values
        System.out.println("Get key1: " + cache.get("key1"));
        Thread.sleep(4000);
        System.out.println("Get key2: " + cache.get("key2")); // Should be null
        System.out.println("Get key1: " + cache.get("key1")); // Should still exist

        // Update an existing entry
        cache.update("key1", "newValue1", 4000);

        // Delete a key
        cache.delete("key1");

        // Add more entries
        cache.put("key3", "value3", 2000); // Expires in 2 seconds
        Thread.sleep(3000);
        System.out.println("Get key3: " + cache.get("key3")); // Should be null

        // Stop the cleaner thread
        cache.stopCleaner();
    }
}
