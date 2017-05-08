package com.think.uiloader.data.store.internal;

/**
 * Created by borney on 3/21/17.
 */

public interface Preference {
    void set(String key, int value);

    int get(String key, int def);

    void set(String key, float value);

    float get(String key, float def);

    void set(String key, long value);

    long get(String key, long def);

    void set(String key, boolean value);

    boolean get(String key, boolean def);

    void set(String key, String value);

    String get(String key, String def);
}
