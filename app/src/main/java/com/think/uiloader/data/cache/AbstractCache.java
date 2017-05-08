package com.think.uiloader.data.cache;

import android.content.Context;

import com.think.uiloader.data.cache.internal.TCache;
import com.think.uiloader.data.cache.serializer.JsonSerializer;
import com.think.uiloader.data.executor.ThreadExecutor;

/**
 * Created by borney on 2/13/17.
 */

abstract class AbstractCache<V> implements Cache<V> {
    Context context;
    ThreadExecutor threadExecutor;
    JsonSerializer jsonSerializer;
    TCache cache;

    AbstractCache(Context context, ThreadExecutor threadExecutor, JsonSerializer jsonSerializer) {
        this.context = context;
        this.threadExecutor = threadExecutor;
        this.jsonSerializer = jsonSerializer;
        this.cache = TCache.get(context);
    }

    void executeAsynchronously(Runnable runnable) {
        threadExecutor.execute(runnable);
    }

    static class CacheWriter implements Runnable {
        private TCache cache;
        private String key;
        private String content;

        CacheWriter(TCache cache, String key, String content) {
            this.cache = cache;
            this.key = key;
            this.content = content;
        }

        @Override
        public void run() {
            cache.putSerializable(key, content);
        }
    }
}
