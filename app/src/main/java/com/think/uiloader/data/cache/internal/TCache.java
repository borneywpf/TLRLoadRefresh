package com.think.uiloader.data.cache.internal;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Looper;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by borney on 3/1/17.
 */

public final class TCache implements CacheManager {
    /**
     * 默认硬盘缓存存储空间大小
     */
    public static final int DEFAULT_MAX_DISK_SPACE = 10 * 1024 * 1024;
    /**
     * 默认硬盘缓存存储空间文件最多个数
     */
    public static final int DEFAULT_MAX_DISK_FILE_COUNT = 100;
    private String cacheDir;
    private Cache diskCacheManager;
    private Cache memoryCacheManager;
    private final static Map<String, TCache> T_CACHE_MAP = new ConcurrentHashMap<>();

    private TCache() {
    }

    public static TCache get(Context context) {
        return get(context, "tcache");
    }

    public static TCache get(Context context, String relativeCacheDir) {
        return get(context, relativeCacheDir, DEFAULT_MAX_DISK_FILE_COUNT, DEFAULT_MAX_DISK_SPACE);
    }

    public static TCache get(Context context, String relativeCacheDir, int maxDiskTotalCount,
            int maxDiskTotalSpace) {
        return get(context, relativeCacheDir, maxDiskTotalCount, maxDiskTotalSpace,
                Integer.MAX_VALUE);
    }

    public static TCache get(Context context, String relativeCacheDir, int maxDiskTotalCount,
            int maxDiskTotalSpace, int defCacheAge) {
        Optional.checkNotNull(context, "context is null !!!");
        checkDirArgument(relativeCacheDir, "relativeCacheDir");
        String cacheDir = context.getFilesDir().getAbsolutePath() + File.separator + relativeCacheDir;
        return getCacheManager(cacheDir, maxDiskTotalCount, maxDiskTotalSpace,
                defCacheAge);
    }

    private static TCache getCacheManager(String cacheDir,
            int maxDiskTotalCount, int maxDiskTotalSpace, int defCacheAge) {
        TCache cache = getTCache(cacheDir);
        cache.cacheDir = cacheDir;
        cache.diskCacheManager = new DiskCacheManager(new FileManager(),
                cache.cacheDir,
                maxDiskTotalCount,
                maxDiskTotalSpace,
                defCacheAge);
        if (cache.memoryCacheManager != null) {
            cache.memoryCacheManager.evictAll();
        } else {
            cache.memoryCacheManager = new MemoryCacheManager();
        }
        return cache;
    }

    private static TCache getTCache(String cacheDir) {
        TCache cache = T_CACHE_MAP.get(cacheDir);
        if (cache == null) {
            cache = new TCache();
            T_CACHE_MAP.put(cacheDir, cache);
        }
        return cache;
    }

    @Override
    public void putBytes(String key, byte[] bytes) {
        putByteMapper(key, bytes, new BytesMapper());
    }

    @Override
    public byte[] getBytes(String key) {
        return getByteMapper(key, new BytesMapper());
    }

    @Override
    public void putBitmap(String key, Bitmap bitmap) {
        putByteMapper(key, bitmap, new BitmapByteMapper());
    }

    @Override
    public Bitmap getBitmap(String key) {
        return getByteMapper(key, new BitmapByteMapper());
    }

    @Override
    public <T extends Serializable> void putSerializable(String key, T obj) {
        putByteMapper(key, obj, new SerializableByteMapper());
    }

    @Override
    public <T extends Serializable> T getSerializable(String key) {
        return (T) getByteMapper(key, new SerializableByteMapper());
    }

    @Override
    public void putJSONObject(String key, JSONObject obj) {
        putSerializable(key, obj.toString());
    }

    @Override
    public JSONObject getJSONObject(String key) throws JSONException {
        return new JSONObject((String) getSerializable(key));
    }

    @Override
    public <T> void putByteMapper(String key, T obj, ByteMapper<T> mapper) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("Can not operate in the main thread !!!");
        }
        Optional.checkNotNull(obj, "obj is null !!!");
        Optional.checkNotNull(mapper, "mapper is null !!!");
        String k = absoluteKey(key);
        diskCacheManager.putByteMapper(k, obj, mapper);
        memoryCacheManager.putByteMapper(k, obj, mapper);
    }

    @Override
    public <T> T getByteMapper(String key, ByteMapper<T> mapper) {
        String k = absoluteKey(key);
        T obj = memoryCacheManager.getByteMapper(k, mapper);
        if (obj == null) {
            obj = diskCacheManager.getByteMapper(k, mapper);
            memoryCacheManager.putByteMapper(k, obj, mapper);
        }
        return obj;
    }

    @Override
    public boolean isExpired(String key) {
        String k = absoluteKey(key);
        return diskCacheManager.isExpired(k);
    }

    @Override
    public boolean isExpired(String key, long age) {
        String k = absoluteKey(key);
        return diskCacheManager.isExpired(k, age);
    }

    @Override
    public synchronized void evict(String key) {
        String k = absoluteKey(key);
        diskCacheManager.evict(k);
        memoryCacheManager.evict(k);
    }

    @Override
    public synchronized void evictAll() {
        diskCacheManager.evictAll();
        memoryCacheManager.evictAll();
    }

    @Override
    public boolean isCached(String key) {
        String k = absoluteKey(key);
        return memoryCacheManager.isCached(k) || diskCacheManager.isCached(k);
    }

    /**
     * recycle current cache manager and memory cache
     */
    @SuppressWarnings("unused")
    public void recycle() {
        memoryCacheManager.evictAll();
        T_CACHE_MAP.remove(cacheDir);
        memoryCacheManager = null;
        diskCacheManager = null;
    }

    /**
     * recycle all tcache
     */
    @SuppressWarnings("unused")
    public static void recycleAll() {
        Iterator<TCache> iterator = T_CACHE_MAP.values().iterator();
        while (iterator.hasNext()) {
            TCache cache = iterator.next();
            cache.memoryCacheManager.evictAll();
            iterator.remove();
            cache.memoryCacheManager = null;
            cache.diskCacheManager = null;
        }
    }

    private String absoluteKey(String key) {
        checkDirArgument(key, "key");
        return new StringBuilder()
                .append(cacheDir)
                .append(File.separator)
                .append(key)
                .toString();
    }

    private static void checkDirArgument(String dir, String tag) {
        if (TextUtils.isEmpty(dir)) {
            throw new IllegalArgumentException(tag + " can not empty!!!");
        }

        String trim = dir.trim();
        if (!trim.equals(dir)) {
            throw new IllegalArgumentException(tag + " not start or end with blank!!");
        }

        if (trim.startsWith(File.separator) || trim.endsWith(File.separator)) {
            throw new IllegalArgumentException(
                    tag + " not start or end with " + File.separator + "!!");
        }
    }
}
