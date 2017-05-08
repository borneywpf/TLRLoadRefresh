package com.think.uiloader.data.cache.internal;

import android.graphics.Bitmap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * @author borney
 * @date 3/1/17
 */

interface CacheManager extends Cache {

    /**
     * cache byte array
     *
     * @param key
     * @param bytes
     */
    void putBytes(String key, byte[] bytes);

    /**
     * get byte array from cache
     *
     * @param key
     * @return
     */
    byte[] getBytes(String key);

    /**
     * cache bitmap
     *
     * @param key
     * @param bitmap
     */
    void putBitmap(String key, Bitmap bitmap);

    /**
     * get bitmap from cache
     *
     * @param key
     * @return
     */
    Bitmap getBitmap(String key);

    /**
     * cache Serializable object
     *
     * @param key
     * @param obj which extends Serializable {@link Serializable}
     * @param <T>
     */
    <T extends Serializable> void putSerializable(String key, T obj);

    /**
     *  get Serializable object from cache
     * @param key
     * @param <T>
     * @return
     */
    <T extends Serializable> T getSerializable(String key);

    /**
     * cache JSONObject
     *
     * @param key
     * @param obj
     */
    void putJSONObject(String key, JSONObject obj);

    /**
     * get JSONObject from cache
     *
     * @param key
     * @return
     * @throws JSONException
     */
    JSONObject getJSONObject(String key) throws JSONException;
}
