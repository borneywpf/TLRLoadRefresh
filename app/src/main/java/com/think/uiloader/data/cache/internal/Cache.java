package com.think.uiloader.data.cache.internal;

/**
 * Created by borney on 3/7/17.
 */

interface Cache {
    /**
     * putByteMapper a object to cache
     *
     * @param key    存储对象文件的相对名称,可以是目录树
     * @param mapper 存储的对象的转换器
     * @param <T>    要存储的对象
     */
    <T> void putByteMapper(String key, T obj, ByteMapper<T> mapper);

    /**
     * get a object from cache
     *
     * @param key 存储对象文件的相对名称,可以是目录树
     * @param <T> 存储的对象的转换器
     * @return 存储的对象
     */
    <T> T getByteMapper(String key, ByteMapper<T> mapper);

    /**
     * cache data is expired or not by key
     *
     * @param key 存储对象文件的相对名称,可以是目录树
     */
    boolean isExpired(String key);

    /**
     * 根据 {@param age} 判断缓存是否过期
     *
     * @param key 存储对象文件的相对名称,可以是目录树
     * @param age 过期指数
     */
    boolean isExpired(String key, long age);

    /**
     * 清除 key 对应的缓存
     *
     * @param key 存储对象文件的相对名称,可以是目录树
     */
    void evict(String key);

    /**
     * 清除所有缓存
     */
    void evictAll();

    /**
     * 是否缓存了key对应的数据
     *
     * @param key
     * @return
     */
    boolean isCached(String key);
}
