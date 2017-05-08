package com.think.uiloader.data.store.internal;

import java.io.Serializable;

/**
 * Created by borney on 3/21/17.
 */

public interface Store extends Preference, Config {
    Preference getPreference(String name);

    Config getConfig(String name);

    <T extends Serializable> Obj<T> getObject(String name);
}
