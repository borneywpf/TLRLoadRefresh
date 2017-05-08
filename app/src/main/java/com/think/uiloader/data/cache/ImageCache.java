package com.think.uiloader.data.cache;

import android.content.Context;

import com.think.uiloader.data.cache.serializer.JsonSerializer;
import com.think.uiloader.data.entity.ImageEntity;
import com.think.uiloader.data.exception.NotFoundException;
import com.think.uiloader.data.executor.ThreadExecutor;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by borney on 5/8/17.
 */
public class ImageCache extends AbstractCache<ImageEntity> {

    @Inject
    ImageCache(Context context, ThreadExecutor threadExecutor, JsonSerializer jsonSerializer) {
        super(context, threadExecutor, jsonSerializer);
    }

    @Override
    public Observable<ImageEntity> get(final String key) {
        return Observable.create(new ObservableOnSubscribe<ImageEntity>() {
            @Override
            public void subscribe(ObservableEmitter<ImageEntity> e) throws Exception {
                String json = cache.getSerializable(key);
                ImageEntity entity = jsonSerializer.deserialize(json, ImageEntity.class);
                if (entity != null) {
                    e.onNext(entity);
                    e.onComplete();
                } else {
                    e.onError(new NotFoundException("not found " + key + " cache"));
                }
            }
        });
    }

    @Override
    public void put(String key, ImageEntity value) {
        String json = jsonSerializer.serialize(value, ImageEntity.class);
        executeAsynchronously(new CacheWriter(cache, key, json));
    }

    @Override
    public boolean isExpired(String key) {
        return cache.isExpired(key);
    }

    @Override
    public void evict(String key) {
        cache.evict(key);
    }

    @Override
    public boolean isCached(String key) {
        return cache.isCached(key);
    }
}
