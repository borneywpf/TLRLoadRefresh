package com.think.uiloader.data.cache;


import io.reactivex.Observable;

/**
 * Created by borney on 2/13/17.
 */

public interface Cache<V> {

    Observable<V> get(String key);

    void put(String key, V value);

    boolean isExpired(String key);

    void evict(String key);

    boolean isCached(String key);
}
