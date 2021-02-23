package com.jinyframework.keva.server.storage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class KevaStoreImpl implements KevaStore {
    private final ConcurrentHashMap<String, Object> keva = new ConcurrentHashMap<>();
    private final Timer timer = new Timer();

    public Object get(String key) {
        return keva.get(key);
    }

    public void put(String key, Object value) {
        keva.put(key, value);
    }

    public void putAll(Map<String, Object> m) {
        keva.putAll(m);
    }

    public void remove(String key) {
        keva.remove(key);
    }

    public void expire(String key, long expireTimeInMilliSecond) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                keva.remove(key);
            }
        }, expireTimeInMilliSecond);
    }

    public Set<Map.Entry<String, Object>> entrySetCopy() {
        return new HashMap<>(keva).entrySet();
    }
}
