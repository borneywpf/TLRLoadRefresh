package com.think.uiloader.data.cache.internal;

/**
 * ByteMapper is a object convert to byte array or byte array to object mapper, It is used to
 * complete the conversion of objects and byte arrays
 *
 * Created by borney on 3/7/17.
 */

public interface ByteMapper<T> {
    /**
     * The byte array of objects
     *
     * @param obj
     * @return
     */
    byte[] getBytes(T obj);

    /**
     * The byte array is converted to an object
     *
     * @param bytes
     * @return
     */
    T getObject(byte[] bytes);
}
