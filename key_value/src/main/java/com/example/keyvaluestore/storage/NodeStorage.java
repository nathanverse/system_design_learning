package com.example.keyvaluestore.storage;

import com.example.keyvaluestore.model.KeyValueEntry;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class NodeStorage {
    private final Map<String, KeyValueEntry> store = new HashMap<>();

    public void put(String key, String value, long timestamp) {
        store.put(key, new KeyValueEntry(key, value, timestamp));
    }

    public KeyValueEntry get(String key) {
        return store.get(key);
    }

    public boolean delete(String key) {
        return store.remove(key) != null;
    }

    public Map<String, KeyValueEntry> getStore() {
        return store;
    }
}