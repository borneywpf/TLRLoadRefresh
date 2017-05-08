package com.think.uiloader.data.store.internal;

import java.io.Serializable;

/**
 * Created by borney on 3/21/17.
 */

public interface Obj<T extends Serializable> {
    void set(T obj);

    T get();
}
